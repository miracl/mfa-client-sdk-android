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
 * @file ecp_BN254CX.h
 * @author Mike Scott
 * @brief ECP Header File
 *
 */

#ifndef ECP_BN254CX_H
#define ECP_BN254CX_H

#include "fp_BN254CX.h"
#include "config_curve_BN254CX.h"

/* Curve Params - see rom_zzz.c */
extern const int CURVE_A_BN254CX;         /**< Elliptic curve A parameter */
extern const int CURVE_B_I_BN254CX;       /**< Elliptic curve B_i parameter */
extern const BIG_256_28 CURVE_B_BN254CX;     /**< Elliptic curve B parameter */
extern const BIG_256_28 CURVE_Order_BN254CX; /**< Elliptic curve group order */
extern const BIG_256_28 CURVE_Cof_BN254CX;   /**< Elliptic curve cofactor */

/* Generator point on G1 */
extern const BIG_256_28 CURVE_Gx_BN254CX; /**< x-coordinate of generator point in group G1  */
extern const BIG_256_28 CURVE_Gy_BN254CX; /**< y-coordinate of generator point in group G1  */


/* For Pairings only */

/* Generator point on G2 */
extern const BIG_256_28 CURVE_Pxa_BN254CX; /**< real part of x-coordinate of generator point in group G2 */
extern const BIG_256_28 CURVE_Pxb_BN254CX; /**< imaginary part of x-coordinate of generator point in group G2 */
extern const BIG_256_28 CURVE_Pya_BN254CX; /**< real part of y-coordinate of generator point in group G2 */
extern const BIG_256_28 CURVE_Pyb_BN254CX; /**< imaginary part of y-coordinate of generator point in group G2 */

extern const BIG_256_28 CURVE_Bnx_BN254CX; /**< BN curve x parameter */

extern const BIG_256_28 CURVE_Cru_BN254CX; /**< BN curve Cube Root of Unity */

extern const BIG_256_28 Fra_BN254CX; /**< real part of BN curve Frobenius Constant */
extern const BIG_256_28 Frb_BN254CX; /**< imaginary part of BN curve Frobenius Constant */


extern const BIG_256_28 CURVE_W_BN254CX[2];	 /**< BN curve constant for GLV decomposition */
extern const BIG_256_28 CURVE_SB_BN254CX[2][2]; /**< BN curve constant for GLV decomposition */
extern const BIG_256_28 CURVE_WB_BN254CX[4];	 /**< BN curve constant for GS decomposition */
extern const BIG_256_28 CURVE_BB_BN254CX[4][4]; /**< BN curve constant for GS decomposition */


/**
	@brief ECP structure - Elliptic Curve Point over base field
*/

typedef struct
{
    int inf; /**< Infinity Flag - not needed for Edwards representation */

    FP_BN254CX x; /**< x-coordinate of point */
#if CURVETYPE_BN254CX!=MONTGOMERY
    FP_BN254CX y; /**< y-coordinate of point. Not needed for Montgomery representation */
#endif
    FP_BN254CX z;/**< z-coordinate of point */
} ECP_BN254CX;


/* ECP E(Fp) prototypes */
/**	@brief Tests for ECP point equal to infinity
 *
	@param P ECP point to be tested
	@return 1 if infinity, else returns 0
 */
extern int ECP_BN254CX_isinf(ECP_BN254CX *P);
/**	@brief Tests for equality of two ECPs
 *
	@param P ECP instance to be compared
	@param Q ECP instance to be compared
	@return 1 if P=Q, else returns 0
 */
extern int ECP_BN254CX_equals(ECP_BN254CX *P,ECP_BN254CX *Q);
/**	@brief Copy ECP point to another ECP point
 *
	@param P ECP instance, on exit = Q
	@param Q ECP instance to be copied
 */
extern void ECP_BN254CX_copy(ECP_BN254CX *P,ECP_BN254CX *Q);
/**	@brief Negation of an ECP point
 *
	@param P ECP instance, on exit = -P
 */
extern void ECP_BN254CX_neg(ECP_BN254CX *P);
/**	@brief Set ECP to point-at-infinity
 *
	@param P ECP instance to be set to infinity
 */
extern void ECP_BN254CX_inf(ECP_BN254CX *P);
/**	@brief Calculate Right Hand Side of curve equation y^2=f(x)
 *
	Function f(x) depends on form of elliptic curve, Weierstrass, Edwards or Montgomery.
	Used internally.
	@param r BIG n-residue value of f(x)
	@param x BIG n-residue x
 */
extern void ECP_BN254CX_rhs(FP_BN254CX *r,FP_BN254CX *x);

#if CURVETYPE_BN254CX==MONTGOMERY
/**	@brief Set ECP to point(x,[y]) given x
 *
	Point P set to infinity if no such point on the curve. Note that y coordinate is not needed.
	@param P ECP instance to be set (x,[y])
	@param x BIG x coordinate of point
	@return 1 if point exists, else 0
 */
extern int ECP_BN254CX_set(ECP_BN254CX *P,BIG_256_28 x);
/**	@brief Extract x coordinate of an ECP point P
 *
	@param x BIG on exit = x coordinate of point
	@param P ECP instance (x,[y])
	@return -1 if P is point-at-infinity, else 0
 */
extern int ECP_BN254CX_get(BIG_256_28 x,ECP_BN254CX *P);
/**	@brief Adds ECP instance Q to ECP instance P, given difference D=P-Q
 *
	Differential addition of points on a Montgomery curve
	@param P ECP instance, on exit =P+Q
	@param Q ECP instance to be added to P
	@param D Difference between P and Q
 */
extern void ECP_BN254CX_add(ECP_BN254CX *P,ECP_BN254CX *Q,ECP_BN254CX *D);
#else
/**	@brief Set ECP to point(x,y) given x and y
 *
	Point P set to infinity if no such point on the curve.
	@param P ECP instance to be set (x,y)
	@param x BIG x coordinate of point
	@param y BIG y coordinate of point
	@return 1 if point exists, else 0
 */
extern int ECP_BN254CX_set(ECP_BN254CX *P,BIG_256_28 x,BIG_256_28 y);
/**	@brief Extract x and y coordinates of an ECP point P
 *
	If x=y, returns only x
	@param x BIG on exit = x coordinate of point
	@param y BIG on exit = y coordinate of point (unless x=y)
	@param P ECP instance (x,y)
	@return sign of y, or -1 if P is point-at-infinity
 */
extern int ECP_BN254CX_get(BIG_256_28 x,BIG_256_28 y,ECP_BN254CX *P);
/**	@brief Adds ECP instance Q to ECP instance P
 *
	@param P ECP instance, on exit =P+Q
	@param Q ECP instance to be added to P
 */
extern void ECP_BN254CX_add(ECP_BN254CX *P,ECP_BN254CX *Q);
/**	@brief Subtracts ECP instance Q from ECP instance P
 *
	@param P ECP instance, on exit =P-Q
	@param Q ECP instance to be subtracted from P
 */
extern void ECP_BN254CX_sub(ECP_BN254CX *P,ECP_BN254CX *Q);
/**	@brief Set ECP to point(x,y) given just x and sign of y
 *
	Point P set to infinity if no such point on the curve. If x is on the curve then y is calculated from the curve equation.
	The correct y value (plus or minus) is selected given its sign s.
	@param P ECP instance to be set (x,[y])
	@param x BIG x coordinate of point
	@param s an integer representing the "sign" of y, in fact its least significant bit.
 */
extern int ECP_BN254CX_setx(ECP_BN254CX *P,BIG_256_28 x,int s);
/**	@brief Maps random BIG to curve point of correct order
 *
	@param Q ECP instance of correct order
	@param w OCTET byte array to be mapped
 */
extern void ECP_BN254CX_mapit(ECP_BN254CX *Q,octet *w);
#endif
/**	@brief Converts an ECP point from Projective (x,y,z) coordinates to affine (x,y) coordinates
 *
	@param P ECP instance to be converted to affine form
 */
extern void ECP_BN254CX_affine(ECP_BN254CX *P);
/**	@brief Formats and outputs an ECP point to the console, in projective coordinates
 *
	@param P ECP instance to be printed
 */
extern void ECP_BN254CX_outputxyz(ECP_BN254CX *P);
/**	@brief Formats and outputs an ECP point to the console, converted to affine coordinates
 *
	@param P ECP instance to be printed
 */
extern void ECP_BN254CX_output(ECP_BN254CX * P);

/**	@brief Formats and outputs an ECP point to the console
 *
	@param P ECP instance to be printed
 */
extern void ECP_BN254CX_rawoutput(ECP_BN254CX * P);

/**	@brief Formats and outputs an ECP point to an octet string
 *
	The octet string is created in the standard form 04|x|y, except for Montgomery curve in which case it is 06|x
	Here x (and y) are the x and y coordinates in big-endian base 256 form.
	@param S output octet string
	@param P ECP instance to be converted to an octet string
 */
extern void ECP_BN254CX_toOctet(octet *S,ECP_BN254CX *P);
/**	@brief Creates an ECP point from an octet string
 *
	The octet string is in the standard form 0x04|x|y, except for Montgomery curve in which case it is 0x06|x
	Here x (and y) are the x and y coordinates in left justified big-endian base 256 form.
	@param P ECP instance to be created from the octet string
	@param S input octet string
	return 1 if octet string corresponds to a point on the curve, else 0
 */
extern int ECP_BN254CX_fromOctet(ECP_BN254CX *P,octet *S);
/**	@brief Doubles an ECP instance P
 *
	@param P ECP instance, on exit =2*P
 */
extern void ECP_BN254CX_dbl(ECP_BN254CX *P);
/**	@brief Multiplies an ECP instance P by a small integer, side-channel resistant
 *
	@param P ECP instance, on exit =i*P
	@param i small integer multiplier
	@param b maximum number of bits in multiplier
 */
extern void ECP_BN254CX_pinmul(ECP_BN254CX *P,int i,int b);
/**	@brief Multiplies an ECP instance P by a BIG, side-channel resistant
 *
	Uses Montgomery ladder for Montgomery curves, otherwise fixed sized windows.
	@param P ECP instance, on exit =b*P
	@param b BIG number multiplier

 */
extern void ECP_BN254CX_mul(ECP_BN254CX *P,BIG_256_28 b);
/**	@brief Calculates double multiplication P=e*P+f*Q, side-channel resistant
 *
	@param P ECP instance, on exit =e*P+f*Q
	@param Q ECP instance
	@param e BIG number multiplier
	@param f BIG number multiplier
 */
extern void ECP_BN254CX_mul2(ECP_BN254CX *P,ECP_BN254CX *Q,BIG_256_28 e,BIG_256_28 f);


#endif
