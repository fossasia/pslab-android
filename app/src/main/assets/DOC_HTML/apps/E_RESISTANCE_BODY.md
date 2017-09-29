Body Resistance
---
### Measure the resistance of the human body

#### Measure the internal resistance of the human body

![](file:///android_asset/DOC_HTML/apps/images/screenshots/bodyResistance.png@100%|auto)

* Ensure that your fingers do not have bruises or cuts!

* The voltage output has been set to 3.0 Volts.  Hold a wire connected to PV3, and with your other hand, hold a wire connected to CH3.

* The input impedance of CH3 is 1MOhm , so a tiny amount of current flows through your body, and also the 1MOhm resistor which is connected internally.

* CH3 measures the voltage drop across the 1MOhm resistor, and therefore, we can calculate the voltage drop across your body (PV3-CH3) . 

#### Calculations

* The current flowing through the circuit , according to Ohm's law, is I = V/R = V(CH3)/1,000,000
* The voltage drop across your body = ( V(PV3)(3.0) - V(CH3)(measured))  
* Body's resistance = voltage drop/current(I)
	
#### Screenshot
![](file:///android_asset/DOC_HTML/apps/images/screenshots/HumanBodyResistance.png@100%|auto)

