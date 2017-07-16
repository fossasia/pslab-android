<html>
  <head>
    <meta content="text/html; charset=windows-1252" http-equiv="content-type">
    <title>index</title>
  </head>
  <body style = "background-color:#fff">Op-Amps : Non-Inverting configuration<br>
	<img src="images/non-inverting.svg" width="100%"><br>

	Make the Connections as shown in the figure.<br>
	CH1 monitors the original waveform output from W1.<br>
	CH2 monitors the amplifier output .<br><br>

	Observe various gains by changing Rf/Ri Ratio.<br>
	If gain is greater than 2x , reduce the input waveform amplitude by
	turning the amplitude knob next to W1 so that the output voltage does not exceed limits.<br>

	<br>Enter Resistance values in the following boxes and click to calculate the theoretical gain
	<form name="form">
	Rf:<input type="text" size="20" name="rf"><br>
	Ri:<input type="text" size="20" name="ri"><br>
	<input type="button" name="B1" value="Calculate" onclick="cal()"><br>
	Gain:<input type="text" size="20" name="answer">
	</form>

	<br>Enter Resistance values in the following boxes and click to calculate the theoretical gain if a capacitor is added in parallel to Rf
	<form name="form2">
	Feedback(Rf):<input type="text" size="20" name="rf"><br>
	Input (Ri)  :<input type="text" size="20" name="ri"><br>
	Capacitance:<input type="text" size="20" name="c"><br>
	Frequency  :<input type="text" size="20" name="f"><br>
	<input type="button" name="B1" value="Calculate" onclick="cal2()"><br>
	Gain:<input type="text" size="20" name="answer">
	</form>

	 
	<script>
	function cal2(){
		var zc,rf,ri,f,c;
		rf = document.form2.rf.value;
		ri = document.form2.ri.value;
		f = document.form2.f.value;
		c = document.form2.c.value;
		zc = 1./(2*3.1415*f*c);
		rf = 1./(1./rf + 1./zc);
		document.form2.answer.value=1+rf/ri;
	}

	function cal(){
		document.form.answer.value=1+document.form.rf.value/document.form.ri.value
	}

	</script>
	
  </body>
</html>
