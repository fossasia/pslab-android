<html>
  <head>
    <meta content="text/html; charset=windows-1252" http-equiv="content-type">
  </head>

  <body>
	<h3>One way traffic for current: The semiconductor Diode</h3>
	<br>CH1 is used to measure the input to the diode, and CH2 will measure the output from it.<br><br>
	<h3>Testing with a DC voltage</h3>
	<img src="images/diodeDC.svg" style="width:100%;max-width:500px;"><br>
	Connect PV1 to the P side of the diode (N side has a black band, P does not ) . Also connect PV1 to CH1 for simultaneous monitoring<br>Use a light emitting diode for more interactive results<br>
	Connect The N side to CH2 to monitor the output voltage. Also connect this end to GND via a high resistance so that some current flow occurs.<br>
	Try different values of PV1, and observe that diodes only allow current flow in one direction. If PV1 is less than GND (0 VOlts), the output voltage is zero.<br> In case of the LED, It will only light up for positive voltages<br><br>
	
	<h3>Testing with an AC voltage</h3>
	<img src="images/diodeAC.svg" style="width:100%;max-width:500px;"><br>
	Repeat the same with a voltage input that is already oscillating, and observe that current only flows when the input exceeds the potential at GND<br>
	Set a very low frequency for W1, such as 10Hz so that you are able to see the LED actually blinking<br>


	<h3>Screenshot</h3>
	<img src="screenshots/diodeSimple.png" width="100%"><br>


  </body>

</html>
