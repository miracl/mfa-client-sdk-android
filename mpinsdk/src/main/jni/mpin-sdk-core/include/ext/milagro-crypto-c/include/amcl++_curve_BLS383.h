#pragma once

#include "amcl++_curve.h"

namespace amcl
{
    class BLS383 : public Curve
    {
    public:
        virtual size_t PGS() const override;
        virtual size_t G1S() const override;
        virtual size_t G2S() const override;
        virtual int GetDvsKeypair(csprng *R, octet *Z, octet *Pa) const override;
        virtual int RecombineG1(octet *Q1, octet *Q2, octet *Q) const override;
        virtual int ExtractFactor(octet *ID, int factor, octet *CS) const override;
        virtual int RestoreFactor(octet *ID, int factor, octet *CS) const override;
        virtual int GetG1Multiple(csprng *R, int type, octet *x, octet *G, octet *W) const override;
        virtual int Client1(int d, octet *ID, csprng *R, octet *x, int pin, octet *T, octet *S, octet *U, octet *UT, octet *TP) const override;
        virtual int Client2(octet *x, octet *y, octet *V) const override;
        virtual int Client(int d, octet *ID, csprng *R, octet *x, int pin, octet *T, octet *V, octet *U, octet *UT, octet *TP, octet* MESSAGE, int t, octet *y) const override;
    };
}
