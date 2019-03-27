/*
	Licensed to the Apache Software Foundation (ASF) under one
	or more contributor license agreements.  See the NOTICE file
	distributed with this work for additional information
	regarding copyright ownership.  The ASF licenses this file
	to you under the Apache License, Version 2.0 (the
	"License"); you may not use this file except in compliance
	with the License.  You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an
	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	KIND, either express or implied.  See the License for the
	specific language governing permissions and limitations
	under the License.
*/

/**
 * @file ecp_BLS383.h
 * @author Mike Scott
 * @brief ECP Header File
 *
 */

#ifndef ECP_BLS383_H
#define ECP_BLS383_H

#include "fp_BLS383.h"
#include "config_curve_BLS383.h"

/* Curve Params - see rom_zzz.c */
extern const int CURVE_A_BLS383;         /**< Elliptic curve A parameter */
extern const int CURVE_B_I_BLS383;       /**< Elliptic curve B_i parameter */
extern const BIG_384_29 CURVE_B_BLS383;     /**< Elliptic curve B parameter */
extern const BIG_384_29 CURVE_Order_BLS383; /**< Elliptic curve group order */
extern const BIG_384_29 CURVE_Cof_BLS383;   /**< Elliptic curve cofactor */

/* Generator point on G1 */
extern const BIG_384_29 CURVE_Gx_BLS383; /**< x-coordinate of generator point in group G1  */
extern const BIG_384_29 CURVE_Gy_BLS383; /**< y-coordinate of generator point in group G1  */


/* For Pairings only */

/* Generator point on G2 */
extern const BIG_384_29 CURVE_Pxa_BLS383; /**< real part of x-coordinate of generator point in group G2 */
extern const BIG_384_29 CURVE_Pxb_BLS383; /**< imaginary part of x-coordinate of generator point in group G2 */
extern const BIG_384_29 CURVE_Pya_BLS383; /**< real part of y-coordinate of generator point in group G2 */
extern const BIG_384_29 CURVE_Pyb_BLS383; /**< imaginary part of y-coordinate of generator point in group G2 */

extern const BIG_384_29 CURVE_Bnx_BLS383; /**< BN curve x parameter */

extern const BIG_384_29 CURVE_Cru_BLS383; /**< BN curve Cube Root of Unity */

extern const BIG_384_29 Fra_BLS383; /**< real part of BN curve Frobenius Constant */
extern const BIG_384_29 Frb_BLS383; /**< imaginary part of BN curve Frobenius Constant */


extern const BIG_384_29 CURVE_W_BLS383[2];	 /**< BN curve constant for GLV decomposition */
extern const BIG_384_29 CURVE_SB_BLS383[2][2]; /**< BN curve constant for GLV decomposition */
extern const BIG_384_29 CURVE_WB_BLS383[4];	 /**< BN curve constant for GS decomposition */
extern const BIG_384_29 CURVE_BB_BLS383[4][4]; /**< BN curve constant for GS decomposition */


/**
	@brief ECP structure - Elliptic Curve Point over base field
*/

typedef struct
{
    int inf; /**< Infinity Flag - not needed for Edwards representation */

    FP_BLS383 x; /**< x-coordinate of point */
#if CURVETYPE_BLS383!=MONTGOMERY
    FP_BLS383 y; /**< y-coordinate of point. Not needed for Montgomery representation */
#endif
    FP_BLS383 z;/**< z-coordinate of point */
} ECP_BLS383;


/* ECP E(Fp) prototypes */
/**	@brief Tests for ECP point equal to infinity
 *
	@param P ECP point to be tested
	@return 1 if infinity, else returns 0
 */
extern int ECP_BLS383_isinf(ECP_BLS383 *P);
/**	@brief Tests for equality of two ECPs
 *
	@param P ECP instance to be compared
	@param Q ECP instance to be compared
	@return 1 if P=Q, else returns 0
 */
extern int ECP_BLS383_equals(ECP_BLS383 *P,ECP_BLS383 *Q);
/**	@brief Copy ECP point to another ECP point
 *
	@param P ECP instance, on exit = Q
	@param Q ECP instance to be copied
 */
extern void ECP_BLS383_copy(ECP_BLS383 *P,ECP_BLS383 *Q);
/**	@brief Negation of an ECP point
 *
	@param P ECP instance, on exit = -P
 */
extern void ECP_BLS383_neg(ECP_BLS383 *P);
/**	@brief Set ECP to point-at-infinity
 *
	@param P ECP instance to be set to infinity
 */
extern void ECP_BLS383_inf(ECP_BLS383 *P);
/**	@brief Calculate Right Hand Side of curve equation y^2=f(x)
 *
	Function f(x) depends on form of elliptic curve, Weierstrass, Edwards or Montgomery.
	Used internally.
	@param r BIG n-residue value of f(x)
	@param x BIG n-residue x
 */
extern void ECP_BLS383_rhs(FP_BLS383 *r,FP_BLS383 *x);

#if CURVETYPE_BLS383==MONTGOMERY
/**	@brief Set ECP to point(x,[y]) given x
 *
	Point P set to infinity if no such point on the curve. Note that y coordinate is not needed.
	@param P ECP instance to be set (x,[y])
	@param x BIG x coordinate of point
	@return 1 if point exists, else 0
 */
extern int ECP_BLS383_set(ECP_BLS383 *P,BIG_384_29 x);
/**	@brief Extract x coordinate of an ECP point P
 *
	@param x BIG on exit = x coordinate of point
	@param P ECP instance (x,[y])
	@return -1 if P is point-at-infinity, else 0
 */
extern int ECP_BLS383_get(BIG_384_29 x,ECP_BLS383 *P);
/**	@brief Adds ECP instance Q to ECP instance P, given difference D=P-Q
 *
	Differential addition of points on a Montgomery curve
	@param P ECP instance, on exit =P+Q
	@param Q ECP instance to be added to P
	@param D Difference between P and Q
 */
extern void ECP_BLS383_add(ECP_BLS383 *P,ECP_BLS383 *Q,ECP_BLS383 *D);
#else
/**	@brief Set ECP to point(x,y) given x and y
 *
	Point P set to infinity if no such point on the curve.
	@param P ECP instance to be set (x,y)
	@param x BIG x coordinate of point
	@param y BIG y coordinate of point
	@return 1 if point exists, else 0
 */
extern int ECP_BLS383_set(ECP_BLS383 *P,BIG_384_29 x,BIG_384_29 y);
/**	@brief Extract x and y coordinates of an ECP point P
 *
	If x=y, returns only x
	@param x BIG on exit = x coordinate of point
	@param y BIG on exit = y coordinate of point (unless x=y)
	@param P ECP instance (x,y)
	@return sign of y, or -1 if P is point-at-infinity
 */
extern int ECP_BLS383_get(BIG_384_29 x,BIG_384_29 y,ECP_BLS383 *P);
/**	@brief Adds ECP instance Q to ECP instance P
 *
	@param P ECP instance, on exit =P+Q
	@param Q ECP instance to be added to P
 */
extern void ECP_BLS383_add(ECP_BLS383 *P,ECP_BLS383 *Q);
/**	@brief Subtracts ECP instance Q from ECP instance P
 *
	@param P ECP instance, on exit =P-Q
	@param Q ECP instance to be subtracted from P
 */
extern void ECP_BLS383_sub(ECP_BLS383 *P,ECP_BLS383 *Q);
/**	@brief Set ECP to point(x,y) given just x and sign of y
 *
	Point P set to infinity if no such point on the curve. If x is on the curve then y is calculated from the curve equation.
	The correct y value (plus or minus) is selected given its sign s.
	@param P ECP instance to be set (x,[y])
	@param x BIG x coordinate of point
	@param s an integer representing the "sign" of y, in fact its least significant bit.
 */
extern int ECP_BLS383_setx(ECP_BLS383 *P,BIG_384_29 x,int s);
/**	@brief Maps random BIG to curve point of correct order
 *
	@param Q ECP instance of correct order
	@param w OCTET byte array to be mapped
 */
extern void ECP_BLS383_mapit(ECP_BLS383 *Q,octet *w);
#endif
/**	@brief Converts an ECP point from Projective (x,y,z) coordinates to affine (x,y) coordinates
 *
	@param P ECP instance to be converted to affine form
 */
extern void ECP_BLS383_affine(ECP_BLS383 *P);
/**	@brief Formats and outputs an ECP point to the console, in projective coordinates
 *
	@param P ECP instance to be printed
 */
extern void ECP_BLS383_outputxyz(ECP_BLS383 *P);
/**	@brief Formats and outputs an ECP point to the console, converted to affine coordinates
 *
	@param P ECP instance to be printed
 */
extern void ECP_BLS383_output(ECP_BLS383 * P);

/**	@brief Formats and outputs an ECP point to the console
 *
	@param P ECP instance to be printed
 */
extern void ECP_BLS383_rawoutput(ECP_BLS383 * P);

/**	@brief Formats and outputs an ECP point to an octet string
 *
	The octet string is created in the standard form 04|x|y, except for Montgomery curve in which case it is 06|x
	Here x (and y) are the x and y coordinates in big-endian base 256 form.
	@param S output octet string
	@param P ECP instance to be converted to an octet string
 */
extern void ECP_BLS383_toOctet(octet *S,ECP_BLS383 *P);
/**	@brief Creates an ECP point from an octet string
 *
	The octet string is in the standard form 0x04|x|y, except for Montgomery curve in which case it is 0x06|x
	Here x (and y) are the x and y coordinates in left justified big-endian base 256 form.
	@param P ECP instance to be created from the octet string
	@param S input octet string
	return 1 if octet string corresponds to a point on the curve, else 0
 */
extern int ECP_BLS383_fromOctet(ECP_BLS383 *P,octet *S);
/**	@brief Doubles an ECP instance P
 *
	@param P ECP instance, on exit =2*P
 */
extern void ECP_BLS383_dbl(ECP_BLS383 *P);
/**	@brief Multiplies an ECP instance P by a small integer, side-channel resistant
 *
	@param P ECP instance, on exit =i*P
	@param i small integer multiplier
	@param b maximum number of bits in multiplier
 */
extern void ECP_BLS383_pinmul(ECP_BLS383 *P,int i,int b);
/**	@brief Multiplies an ECP instance P by a BIG, side-channel resistant
 *
	Uses Montgomery ladder for Montgomery curves, otherwise fixed sized windows.
	@param P ECP instance, on exit =b*P
	@param b BIG number multiplier

 */
extern void ECP_BLS383_mul(ECP_BLS383 *P,BIG_384_29 b);
/**	@brief Calculates double multiplication P=e*P+f*Q, side-channel resistant
 *
	@param P ECP instance, on exit =e*P+f*Q
	@param Q ECP instance
	@param e BIG number multiplier
	@param f BIG number multiplier
 */
extern void ECP_BLS383_mul2(ECP_BLS383 *P,ECP_BLS383 *Q,BIG_384_29 e,BIG_384_29 f);


#endif
