package org.micromanager.plugins.fluidicsequencer.sequencebuilder;

import com.google.gson.annotations.SerializedName;

public enum ValueUnit {
   // Pressure units
   @SerializedName("Pa")
   Pa,
   @SerializedName("kPa")
   kPa,
   @SerializedName("hPa")
   hPa,
   @SerializedName("bar")
   bar,
   @SerializedName("mbar")
   mbar,
   @SerializedName("atm")
   atm,
   @SerializedName("psi")
   psi,

   // Flow rate units
   @SerializedName("uL_s")
   uL_s,
   @SerializedName("uL_m")
   uL_m,
   @SerializedName("uL_h")
   uL_h,
   @SerializedName("mL_s")
   mL_s,
   @SerializedName("mL_m")
   mL_m,
   @SerializedName("mL_h")
   mL_h
}
// {"Pa", 0.001},
// {"kPa", 1},
// {"hPa", 100},
// {"bar", 100},
// {"mbar", 0.1},
// {"atm", 101.325},
// {"psi", 6.89476}
