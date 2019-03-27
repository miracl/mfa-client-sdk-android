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
 * @file ecp2_BN254CX.h
 * @author Mike Scott
 * @brief ECP2 Header File
 *
 */

#ifndef ECP2_BN254CX_H
#define ECP2_BN254CX_H

#include "fp2_BN254CX.h"
#include "config_curve_BN254CX.h"

/**
	@brief ECP2 Structure - Elliptic Curve Point over quadratic extension field
*/

typedef struct
{
    int inf; /**< Infinity Flag */
    FP2_BN254CX x;   /**< x-coordinate of point */
    FP2_BN254CX y;   /**< y-coordinate of point */
    FP2_BN254CX z;   /**< z-coordinate of point */
} ECP2_BN254CX;


/* Curve Params - see rom_zzz.c */
extern const int CURVE_A_BN254CX;		/**< Elliptic curve A parameter */
extern const int CURVE_B_I_BN254CX;		/**< Elliptic curve B parameter */
extern const BIG_256_28 CURVE_B_BN254CX;     /**< Elliptic curve B parameter */
extern const BIG_256_28 CURVE_Order_BN254CX; /**< Elliptic curve group order */
extern const BIG_256_28 CURVE_Cof_BN254CX;   /**< Elliptic curve cofactor */
extern const BIG_256_28 CURVE_Bnx_BN254CX;   /**< Elliptic curve parameter */

extern const BIG_256_28 Fra_BN254CX; /**< real part of BN curve Frobenius Constant */
extern const BIG_256_28 Frb_BN254CX; /**< imaginary part of BN curve Frobenius Constant */


/* Generator point on G1 */
extern const BIG_256_28 CURVE_Gx_BN254CX; /**< x-coordinate of generator point in group G1  */
extern const BIG_256_28 CURVE_Gy_BN254CX; /**< y-coordinate of generator point in group G1  */

/* For Pairings only */

/* Generator point on G2 */
extern const BIG_256_28 CURVE_Pxa_BN254CX; /**< real part of x-coordinate of generator point in group G2 */
extern const BIG_256_28 CURVE_Pxb_BN254CX; /**< imaginary part of x-coordinate of generator point in group G2 */
extern const BIG_256_28 CURVE_Pya_BN254CX; /**< real part of y-coordinate of generator point in group G2 */
extern const BIG_256_28 CURVE_Pyb_BN254CX; /**< imaginary part of y-coordinate of generator point in group G2 */

/* ECP2 E(Fp2) prototypes */
/**	@brief Tests for ECP2 point equal to infinity
 *
	@param P ECP2 point to be tested
	@return 1 if infinity, else returns 0
 */
extern int ECP2_BN254CX_isinf(ECP2_BN254CX *P);
/**	@brief Copy ECP2 point to another ECP2 point
 *
	@param P ECP2 instance, on exit = Q
	@param Q ECP2 instance to be copied
 */
extern void ECP2_BN254CX_copy(ECP2_BN254CX *P,ECP2_BN254CX *Q);
/**	@brief Set ECP2 to point-at-infinity
 *
	@param P ECP2 instance to be set to infinity
 */
extern void ECP2_BN254CX_inf(ECP2_BN254CX *P);
/**	@brief Tests for equality of two ECP2s
 *
	@param P ECP2 instance to be compared
	@param Q ECP2 instance to be compared
	@return 1 if P=Q, else returns 0
 */
extern int ECP2_BN254CX_equals(ECP2_BN254CX *P,ECP2_BN254CX *Q);
/**	@brief Converts an ECP2 point from Projective (x,y,z) coordinates to affine (x,y) coordinates
 *
	@param P ECP2 instance to be converted to affine form
 */
extern void ECP2_BN254CX_affine(ECP2_BN254CX *P);
/**	@brief Extract x and y coordinates of an ECP2 point P
 *
	If x=y, returns only x
	@param x FP2 on exit = x coordinate of point
	@param y FP2 on exit = y coordinate of point (unless x=y)
	@param P ECP2 instance (x,y)
	@return -1 if P is point-at-infinity, else 0
 */
extern int ECP2_BN254CX_get(FP2_BN254CX *x,FP2_BN254CX *y,ECP2_BN254CX *P);
/**	@brief Formats and outputs an ECP2 point to the console, converted to affine coordinates
 *
	@param P ECP2 instance to be printed
 */
extern void ECP2_BN254CX_output(ECP2_BN254CX *P);
/**	@brief Formats and outputs an ECP2 point to the console, in projective coordinates
 *
	@param P ECP2 instance to be printed
 */
extern void ECP2_BN254CX_outputxyz(ECP2_BN254CX *P);
/**	@brief Formats and outputs an ECP2 point to an octet string
 *
	The octet string is created in the form x|y.
	Convert the real and imaginary parts of the x and y coordinates to big-endian base 256 form.
	@param S output octet string
	@param P ECP2 instance to be converted to an octet string
 */
extern void ECP2_BN254CX_toOctet(octet *S,ECP2_BN254CX *P);
/**	@brief Creates an ECP2 point from an octet string
 *
	The octet string is in the form x|y
	The real and imaginary parts of the x and y coordinates are in big-endian base 256 form.
	@param P ECP2 instance to be created from the octet string
	@param S input octet string
	return 1 if octet string corresponds to a point on the curve, else 0
 */
extern int ECP2_BN254CX_fromOctet(ECP2_BN254CX *P,octet *S);
/**	@brief Calculate Right Hand Side of curve equation y^2=f(x)
 *
	Function f(x)=x^3+Ax+B
	Used internally.
	@param r FP2 value of f(x)
	@param x FP2 instance
 */
extern void ECP2_BN254CX_rhs(FP2_BN254CX *r,FP2_BN254CX *x);
/**	@brief Set ECP2 to point(x,y) given x and y
 *
	Point P set to infinity if no such point on the curve.
	@param P ECP2 instance to be set (x,y)
	@param x FP2 x coordinate of point
	@param y FP2 y coordinate of point
	@return 1 if point exists, else 0
 */
extern int ECP2_BN254CX_set(ECP2_BN254CX *P,FP2_BN254CX *x,FP2_BN254CX *y);
/**	@brief Set ECP to point(x,[y]) given x
 *
	Point P set to infinity if no such point on the curve. Otherwise y coordinate is calculated from x.
	@param P ECP instance to be set (x,[y])
	@param x BIG x coordinate of point
	@return 1 if point exists, else 0
 */
extern int ECP2_BN254CX_setx(ECP2_BN254CX *P,FP2_BN254CX *x);
/**	@brief Negation of an ECP2 point
 *
	@param P ECP2 instance, on exit = -P
 */
extern void ECP2_BN254CX_neg(ECP2_BN254CX *P);
/**	@brief Doubles an ECP2 instance P
 *
	@param P ECP2 instance, on exit =2*P
 */
extern int ECP2_BN254CX_dbl(ECP2_BN254CX *P);
/**	@brief Adds ECP2 instance Q to ECP2 instance P
 *
	@param P ECP2 instance, on exit =P+Q
	@param Q ECP2 instance to be added to P
 */
extern int ECP2_BN254CX_add(ECP2_BN254CX *P,ECP2_BN254CX *Q);
/**	@brief Subtracts ECP instance Q from ECP2 instance P
 *
	@param P ECP2 instance, on exit =P-Q
	@param Q ECP2 instance to be subtracted from P
 */
extern void ECP2_BN254CX_sub(ECP2_BN254CX *P,ECP2_BN254CX *Q);
/**	@brief Multiplies an ECP2 instance P by a BIG, side-channel resistant
 *
	Uses fixed sized windows.
	@param P ECP2 instance, on exit =b*P
	@param b BIG number multiplier

 */
extern void ECP2_BN254CX_mul(ECP2_BN254CX *P,BIG_256_28 b);
/**	@brief Multiplies an ECP2 instance P by the internal modulus p, using precalculated Frobenius constant f
 *
	Fast point multiplication using Frobenius
	@param P ECP2 instance, on exit = p*P
	@param f FP2 precalculated Frobenius constant

 */
extern void ECP2_BN254CX_frob(ECP2_BN254CX *P,FP2_BN254CX *f);
/**	@brief Calculates P=b[0]*Q[0]+b[1]*Q[1]+b[2]*Q[2]+b[3]*Q[3]
 *
	@param P ECP2 instance, on exit = b[0]*Q[0]+b[1]*Q[1]+b[2]*Q[2]+b[3]*Q[3]
	@param Q ECP2 array of 4 points
	@param b BIG array of 4 multipliers
 */
extern void ECP2_BN254CX_mul4(ECP2_BN254CX *P,ECP2_BN254CX *Q,BIG_256_28 *b);

/**	@brief Maps random BIG to curve point of correct order
 *
	@param P ECP2 instance of correct order
	@param w OCTET byte array to be mapped
 */
extern void ECP2_BN254CX_mapit(ECP2_BN254CX *P,octet *w);

#endif
