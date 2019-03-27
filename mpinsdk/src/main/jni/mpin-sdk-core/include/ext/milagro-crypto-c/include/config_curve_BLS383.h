#ifndef CONFIG_CURVE_BLS383_H
#define CONFIG_CURVE_BLS383_H

#include"amcl.h"
#include"config_field_BLS383.h"

// ECP stuff

#define CURVETYPE_BLS383 WEIERSTRASS
#define PAIRING_FRIENDLY_BLS383 BLS

/*
#define CURVETYPE_BLS383 EDWARDS
#define PAIRING_FRIENDLY_BLS383 NOT
*/

#if PAIRING_FRIENDLY_BLS383 != NOT
//#define USE_GLV_BLS383	  /**< Note this method is patented (GLV), so maybe you want to comment this out */
//#define USE_GS_G2_BLS383 /**< Well we didn't patent it :) But may be covered by GLV patent :( */
#define USE_GS_GT_BLS383 /**< Not patented, so probably safe to always use this */

#define POSITIVEX 0
#define NEGATIVEX 1

#define SEXTIC_TWIST_BLS383 M_TYPE
#define SIGN_OF_X_BLS383 POSITIVEX

#endif

#endif
