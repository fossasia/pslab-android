use anyhow::{anyhow, Result};
use crate::frb_generated::StreamSink;
use crate::api::bootloader::FlashState;

#[cfg(all(not(target_family = "wasm"), not(target_os = "ios")))]
use rusb::{request_type, Direction, Recipient, RequestType, DeviceHandle, GlobalContext, UsbContext};
#[cfg(all(not(target_family = "wasm"), not(target_os = "ios")))]
use std::time::Duration;
#[cfg(all(not(target_family = "wasm"), not(target_os = "ios")))]
use crate::api::common::USB_HANDLE;

const DFU_DNLOAD: u8 = 1;
const DFU_GETSTATUS: u8 = 3;
const DFU_CLRSTATUS: u8 = 4;

const FLIP2_CMD_GROUP_DOWNLOAD: u8 = 0x01;
const FLIP2_CMD_PROG_START: u8 = 0x00;

const FLIP2_CMD_GROUP_EXEC: u8 = 0x04;
const FLIP2_CMD_CHIP_ERASE: u8 = 0x00;
const FLIP2_CMD_START_APP: u8 = 0x03;

const FLIP2_CMD_GROUP_SELECT: u8 = 0x06;
const FLIP2_CMD_SELECT_MEMORY: u8 = 0x03;

#[cfg(all(not(target_family = "wasm"), not(target_os = "ios")))]
fn clr_status(handle: &DeviceHandle<GlobalContext>) -> Result<()> {
    let timeout = Duration::from_millis(1000);
    let _ = handle.write_control(
        request_type(Direction::Out, RequestType::Class, Recipient::Interface),
        DFU_CLRSTATUS,
        0,
        0,
        &[],
        timeout,
    );
    Ok(())
}

#[cfg(all(not(target_family = "wasm"), not(target_os = "ios")))]
fn get_status(handle: &DeviceHandle<GlobalContext>) -> Result<(u8, u8)> {
    let timeout = Duration::from_millis(1000);
    let mut buf = [0u8; 6];
    let n = handle.read_control(
        request_type(Direction::In, RequestType::Class, Recipient::Interface),
        DFU_GETSTATUS,
        0,
        0,
        &mut buf,
        timeout,
    ).map_err(|e| anyhow!("DFU_GETSTATUS error: {}", e))?;
    if n < 6 {
        return Err(anyhow!("DFU_GETSTATUS returned short response ({} bytes)", n));
    }
    Ok((buf[0], buf[4])) 
}

#[cfg(all(not(target_family = "wasm"), not(target_os = "ios")))]
fn dnload(handle: &DeviceHandle<GlobalContext>, block_num: u16, data: &[u8]) -> Result<()> {
    let timeout = Duration::from_millis(2000);
    let n = handle.write_control(
        request_type(Direction::Out, RequestType::Class, Recipient::Interface),
        DFU_DNLOAD,
        block_num,
        0,
        data,
        timeout,
    ).map_err(|e| anyhow!("DFU_DNLOAD error: {}", e))?;
    if n != data.len() {
        return Err(anyhow!("DFU_DNLOAD short write (wrote {} of {} bytes)", n, data.len()));
    }
    Ok(())
}

fn parse_hex_flash_image(hex_str: &str) -> Result<(Vec<u8>, usize)> {
    let mut flash = vec![0xFFu8; 32768];
    let mut max_addr = 0usize;
    let mut ext_addr = 0usize;

    for record in ihex::Reader::new(hex_str) {
        let rec = record.map_err(|e| anyhow!("Intel HEX parse error: {:?}", e))?;
        match rec {
            ihex::Record::ExtendedLinearAddress(addr) => {
                ext_addr = (addr as usize) << 16;
            }
            ihex::Record::ExtendedSegmentAddress(addr) => {
                ext_addr = (addr as usize) << 4;
            }
            ihex::Record::Data { offset, value } => {
                let base = ext_addr + (offset as usize);
                for (i, &b) in value.iter().enumerate() {
                    let target = base + i;
                    if target < 32768 {
                        flash[target] = b;
                        if target + 1 > max_addr {
                            max_addr = target + 1;
                        }
                    }
                }
            }
            ihex::Record::EndOfFile => break,
            _ => {}
        }
    }

    if max_addr == 0 {
        return Err(anyhow!("Intel HEX image contains no flash records"));
    }

    Ok((flash, max_addr))
}

pub fn flash_labrador_firmware(hex_str: String, progress_sink: StreamSink<FlashState>) -> Result<()> {
    let _ = progress_sink.add(FlashState::Connecting);

    #[cfg(any(target_family = "wasm", target_os = "ios"))]
    {
        let _ = hex_str;
        let _ = progress_sink.add(FlashState::Error {
            message: "Native DFU flasher is not available on this platform".to_string(),
        });
        Ok(())
    }

    #[cfg(all(not(target_family = "wasm"), not(target_os = "ios")))]
    {
        match flash_labrador_inner(&hex_str, &progress_sink) {
            Ok(_) => Ok(()),
            Err(e) => {
                let err_msg = format!("{}", e);
                let _ = progress_sink.add(FlashState::Error { message: err_msg });
                Ok(())
            }
        }
    }
}

#[cfg(all(not(target_family = "wasm"), not(target_os = "ios")))]
fn flash_labrador_inner(hex_str: &str, progress_sink: &StreamSink<FlashState>) -> Result<()> {
    let handle = {
        let guard = USB_HANDLE.lock().unwrap();
        if let Some(h) = guard.as_ref() {
            h.clone()
        } else {
            let context = GlobalContext::default();
            let h = context
                .open_device_with_vid_pid(0x03EB, 0x2FE4)
                .ok_or_else(|| {
                    anyhow!(
                        "No Labrador board detected in DFU bootloader mode (VID 03EB, PID 2FE4).\n\n\
                        Please connect DO1 to GND using a jumper wire while plugging into USB, or click 'Jump Connected Board to Bootloader'."
                    )
                })?;
            std::sync::Arc::new(h)
        }
    };

    let _ = handle.set_auto_detach_kernel_driver(true);
    let _ = handle.claim_interface(0);

    let mut block_counter: u16 = 0;
    clr_status(&handle)?;
    if let Ok((b_status, _)) = get_status(&handle) {
        if b_status != 0 {
            clr_status(&handle)?;
        }
    }

    let _ = progress_sink.add(FlashState::Erasing);
    let erase_cmd = [FLIP2_CMD_GROUP_EXEC, FLIP2_CMD_CHIP_ERASE, 0xFF, 0x00, 0x00, 0x00];
    dnload(&handle, block_counter, &erase_cmd)?;
    block_counter += 1;

    let mut erase_retries = 30;
    while erase_retries > 0 {
        std::thread::sleep(Duration::from_millis(50));
        let (s_stat, s_st) = get_status(&handle)?;
        if s_stat == 0 {
            break;
        }
        if s_stat == 0x09 && s_st == 0x04 {
            erase_retries -= 1;
            continue;
        }
        clr_status(&handle)?;
        erase_retries -= 1;
    }
    let select_mem_cmd = [FLIP2_CMD_GROUP_SELECT, FLIP2_CMD_SELECT_MEMORY, 0x00, 0x00, 0x00, 0x00];
    dnload(&handle, block_counter, &select_mem_cmd)?;
    block_counter += 1;
    let (stat, _) = get_status(&handle)?;
    if stat != 0 {
        return Err(anyhow!("Failed to select flash memory unit (status: 0x{:02X})", stat));
    }
    let select_page_cmd = [FLIP2_CMD_GROUP_SELECT, FLIP2_CMD_SELECT_MEMORY, 0x01, 0x00, 0x00, 0x00];
    dnload(&handle, block_counter, &select_page_cmd)?;
    block_counter += 1;
    let (stat, _) = get_status(&handle)?;
    if stat != 0 {
        return Err(anyhow!("Failed to select memory page 0 (status: 0x{:02X})", stat));
    }
    let (flash_image, max_addr) = parse_hex_flash_image(hex_str)?;
    let mut end_addr = max_addr;
    let rem = end_addr % 512;
    if rem != 0 {
        end_addr += 512 - rem;
    }
    if end_addr > 32768 {
        end_addr = 32768;
    }

    let mut written = 0usize;
    let total_bytes = end_addr;

    for offset in (0..end_addr).step_by(512) {
        let chunk_size = std::cmp::min(512, end_addr - offset);

        let start_hi = ((offset >> 8) & 0xFF) as u8;
        let start_lo = (offset & 0xFF) as u8;
        let end_hi = (((offset + chunk_size - 1) >> 8) & 0xFF) as u8;
        let end_lo = ((offset + chunk_size - 1) & 0xFF) as u8;

        let cmd = [
            FLIP2_CMD_GROUP_DOWNLOAD,
            FLIP2_CMD_PROG_START,
            start_hi,
            start_lo,
            end_hi,
            end_lo,
        ];

        let mut buffer = vec![0u8; 64 + chunk_size];
        buffer[0..6].copy_from_slice(&cmd);
        buffer[64..64 + chunk_size].copy_from_slice(&flash_image[offset..offset + chunk_size]);

        dnload(&handle, block_counter, &buffer)?;
        block_counter += 1;

        let (stat, _) = get_status(&handle)?;
        if stat != 0 {
            return Err(anyhow!("Flash write failed at offset 0x{:04X} (status: 0x{:02X})", offset, stat));
        }

        written += chunk_size;
        let percent = ((written as f32 / total_bytes as f32) * 100.0) as i32;
        let _ = progress_sink.add(FlashState::Writing { progress_percent: percent });
    }

    let _ = progress_sink.add(FlashState::Verifying);
    std::thread::sleep(Duration::from_millis(50));
    let launch_cmd = [FLIP2_CMD_GROUP_EXEC, FLIP2_CMD_START_APP, 0x00, 0x00, 0x00, 0x00];
    let _ = dnload(&handle, block_counter, &launch_cmd);
    block_counter += 1;
    std::thread::sleep(Duration::from_millis(100));
    let _ = dnload(&handle, block_counter, &launch_cmd);

    let _ = handle.release_interface(0);

    let _ = progress_sink.add(FlashState::Finished);
    Ok(())
}
