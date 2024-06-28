default_platform(:android)
platform :android do
  lane :uploadToOpenTesting do
    upload_to_play_store(track: "beta",aab:"app/build/outputs/bundle/release/app-release.aab")
  end

  lane :promoteToProduction do
    supply(track: "beta",track_promote_to: "production",skip_upload_apk: true)
  end
end
