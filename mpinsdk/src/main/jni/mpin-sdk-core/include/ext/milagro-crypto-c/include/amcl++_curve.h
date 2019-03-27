#pragma once

#define C99
extern "C"
{
#include <amcl.h>
}

namespace amcl
{
    class Curve
    {
    public:
        virtual size_t PGS() const = 0;
        virtual size_t G1S() const = 0;
        virtual size_t G2S() const = 0;
        virtual int GetDvsKeypair(csprng *R, octet *Z, octet *Pa) const = 0;
        virtual int RecombineG1(octet *Q1, octet *Q2, octet *Q) const = 0;
        virtual int ExtractFactor(octet *ID, int factor, octet *CS) const = 0;
        virtual int RestoreFactor(octet *ID, int factor, octet *CS) const = 0;
        virtual int GetG1Multiple(csprng *R, int type, octet *x, octet *G, octet *W) const = 0;
        virtual int Client1(int d, octet *ID, csprng *R, octet *x, int pin, octet *T, octet *S, octet *U, octet *UT, octet *TP) const = 0;
        virtual int Client2(octet *x, octet *y, octet *V) const = 0;
        virtual int Client(int d, octet *ID, csprng *R, octet *x, int pin, octet *T, octet *V, octet *U, octet *UT, octet *TP, octet* MESSAGE, int t, octet *y) const = 0;
    };
}
