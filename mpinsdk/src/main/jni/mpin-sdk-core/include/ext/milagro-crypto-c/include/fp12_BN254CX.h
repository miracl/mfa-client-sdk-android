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
 * @file fp12_BN254CX.h
 * @author Mike Scott
 * @brief FP12 Header File
 *
 */

#ifndef FP12_BN254CX_H
#define FP12_BN254CX_H

#include "fp4_BN254CX.h"

/**
	@brief FP12 Structure - towered over three FP4
*/

typedef struct
{
    FP4_BN254CX a; /**< first part of FP12 */
    FP4_BN254CX b; /**< second part of FP12 */
    FP4_BN254CX c; /**< third part of FP12 */
} FP12_BN254CX;

extern const BIG_256_28 Fra_BN254CX; /**< real part of BN curve Frobenius Constant */
extern const BIG_256_28 Frb_BN254CX; /**< imaginary part of BN curve Frobenius Constant */

/* FP12 prototypes */
/**	@brief Tests for FP12 equal to zero
 *
	@param x FP12 number to be tested
	@return 1 if zero, else returns 0
 */
extern int FP12_BN254CX_iszilch(FP12_BN254CX *x);
/**	@brief Tests for FP12 equal to unity
 *
	@param x FP12 number to be tested
	@return 1 if unity, else returns 0
 */
extern int FP12_BN254CX_isunity(FP12_BN254CX *x);
/**	@brief Copy FP12 to another FP12
 *
	@param x FP12 instance, on exit = y
	@param y FP12 instance to be copied
 */
extern void FP12_BN254CX_copy(FP12_BN254CX *x,FP12_BN254CX *y);
/**	@brief Set FP12 to unity
 *
	@param x FP12 instance to be set to one
 */
extern void FP12_BN254CX_one(FP12_BN254CX *x);
/**	@brief Tests for equality of two FP12s
 *
	@param x FP12 instance to be compared
	@param y FP12 instance to be compared
	@return 1 if x=y, else returns 0
 */
extern int FP12_BN254CX_equals(FP12_BN254CX *x,FP12_BN254CX *y);
/**	@brief Conjugation of FP12
 *
	If y=(a,b,c) (where a,b,c are its three FP4 components) on exit x=(conj(a),-conj(b),conj(c))
	@param x FP12 instance, on exit = conj(y)
	@param y FP12 instance
 */
extern void FP12_BN254CX_conj(FP12_BN254CX *x,FP12_BN254CX *y);
/**	@brief Initialise FP12 from single FP4
 *
	Sets first FP4 component of an FP12, other components set to zero
	@param x FP12 instance to be initialised
	@param a FP4 to form first part of FP4
 */
extern void FP12_BN254CX_from_FP4(FP12_BN254CX *x,FP4_BN254CX *a);
/**	@brief Initialise FP12 from three FP4s
 *
	@param x FP12 instance to be initialised
	@param a FP4 to form first part of FP12
	@param b FP4 to form second part of FP12
	@param c FP4 to form third part of FP12
 */
extern void FP12_BN254CX_from_FP4s(FP12_BN254CX *x,FP4_BN254CX *a,FP4_BN254CX* b,FP4_BN254CX *c);
/**	@brief Fast Squaring of an FP12 in "unitary" form
 *
	@param x FP12 instance, on exit = y^2
	@param y FP4 instance, must be unitary
 */
extern void FP12_BN254CX_usqr(FP12_BN254CX *x,FP12_BN254CX *y);
/**	@brief Squaring an FP12
 *
	@param x FP12 instance, on exit = y^2
	@param y FP12 instance
 */
extern void FP12_BN254CX_sqr(FP12_BN254CX *x,FP12_BN254CX *y);
/**	@brief Fast multiplication of an FP12 by an FP12 that arises from an ATE pairing line function
 *
	Here the multiplier has a special form that can be exploited
	@param x FP12 instance, on exit = x*y
	@param y FP12 instance, of special form
	@param t D_TYPE or M_TYPE twist
 */
extern void FP12_BN254CX_smul(FP12_BN254CX *x,FP12_BN254CX *y,int t);
/**	@brief Multiplication of two FP12s
 *
	@param x FP12 instance, on exit = x*y
	@param y FP12 instance, the multiplier
 */
extern void FP12_BN254CX_mul(FP12_BN254CX *x,FP12_BN254CX *y);
/**	@brief Inverting an FP12
 *
	@param x FP12 instance, on exit = 1/y
	@param y FP12 instance
 */
extern void FP12_BN254CX_inv(FP12_BN254CX *x,FP12_BN254CX *y);
/**	@brief Raises an FP12 to the power of a BIG
 *
	@param r FP12 instance, on exit = y^b
	@param x FP12 instance
	@param b BIG number
 */
extern void FP12_BN254CX_pow(FP12_BN254CX *r,FP12_BN254CX *x,BIG_256_28 b);
/**	@brief Raises an FP12 instance x to a small integer power, side-channel resistant
 *
	@param x FP12 instance, on exit = x^i
	@param i small integer exponent
	@param b maximum number of bits in exponent
 */
extern void FP12_BN254CX_pinpow(FP12_BN254CX *x,int i,int b);

/**	@brief Raises an FP12 instance x to a BIG power, compressed to FP4
 *
	@param c FP4 instance, on exit = x^(e mod r) as FP4
	@param x FP12 input
	@param e BIG exponent
	@param r BIG group order
 */
extern void FP12_BN254CX_compow(FP4_BN254CX *c,FP12_BN254CX *x,BIG_256_28 e,BIG_256_28 r);

/**	@brief Calculate x[0]^b[0].x[1]^b[1].x[2]^b[2].x[3]^b[3], side-channel resistant
 *
	@param r FP12 instance, on exit = x[0]^b[0].x[1]^b[1].x[2]^b[2].x[3]^b[3]
	@param x FP12 array with 4 FP12s
	@param b BIG array of 4 exponents
 */
extern void FP12_BN254CX_pow4(FP12_BN254CX *r,FP12_BN254CX *x,BIG_256_28 *b);
/**	@brief Raises an FP12 to the power of the internal modulus p, using the Frobenius
 *
	@param x FP12 instance, on exit = x^p
	@param f FP2 precalculated Frobenius constant
 */
extern void FP12_BN254CX_frob(FP12_BN254CX *x,FP2_BN254CX *f);
/**	@brief Reduces all components of possibly unreduced FP12 mod Modulus
 *
	@param x FP12 instance, on exit reduced mod Modulus
 */
extern void FP12_BN254CX_reduce(FP12_BN254CX *x);
/**	@brief Normalises the components of an FP12
 *
	@param x FP12 instance to be normalised
 */
extern void FP12_BN254CX_norm(FP12_BN254CX *x);
/**	@brief Formats and outputs an FP12 to the console
 *
	@param x FP12 instance to be printed
 */
extern void FP12_BN254CX_output(FP12_BN254CX *x);
/**	@brief Formats and outputs an FP12 instance to an octet string
 *
	Serializes the components of an FP12 to big-endian base 256 form.
	@param S output octet string
	@param x FP12 instance to be converted to an octet string
 */
extern void FP12_BN254CX_toOctet(octet *S,FP12_BN254CX *x);
/**	@brief Creates an FP12 instance from an octet string
 *
	De-serializes the components of an FP12 to create an FP12 from big-endian base 256 components.
	@param x FP12 instance to be created from an octet string
	@param S input octet string

 */
extern void FP12_BN254CX_fromOctet(FP12_BN254CX *x,octet *S);
/**	@brief Calculate the trace of an FP12
 *
	@param t FP4 trace of x, on exit = tr(x)
	@param x FP12 instance

 */
extern void FP12_BN254CX_trace(FP4_BN254CX *t,FP12_BN254CX *x);

#endif
