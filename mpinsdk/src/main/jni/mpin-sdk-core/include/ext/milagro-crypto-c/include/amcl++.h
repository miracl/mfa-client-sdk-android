#pragma once

#define C99
extern "C"
{
#include <randapi.h>
}
#include "amcl++_curve.h"
#include <string>
#include <vector>
#include <map>
#include <memory>

namespace amcl
{
    using string = std::string;
    using MultiFactor = std::vector<int>;

    class Rng
    {
    public:
        Rng(const string& seed);
        ~Rng();
        operator csprng*();

    private:
        csprng m_rng;
    };

    class Status
    {
    public:
        int code;
        string error;

        static const int OK = 0;

        Status();
        Status(int _code);
        Status(int _code, const string& func);
        Status(int _code, const string& func, const string& _error);
        bool operator==(int _code) const;
        bool operator!=(int _code) const;
    };

    string DvsHash(const string& id);

    class MPin
    {
    public:
        MPin(const string& seed);
        Status GetDvsKeypair(const string& curve, string& publicKey, string& privateKey);
        Status RecombineG1(const string& curve, const string& cs1, const string& cs2, string& result) const;
        Status ExtractFactors(const string& curve, const string& id, const MultiFactor& factors, const string& clientSecret, string& result) const;
        Status GetG1Multiple(const string& curve, const string& privateKey, const string& clientSecret, string& result) const;

        class Client1Result
        {
        public:
            string x;
            string sec;
            string u;
            string ut;
        };
        Status Client1(const string& curve, const string& id, const MultiFactor& factors, const string& token, int date, const string& timePermit, Client1Result& result);

        Status Client2(const string& curve, const string& x, const string& y, const string& sec, string& result) const;

        class ClientResult
        {
        public:
            string u;
            string sec;
        };
        Status Client(const string& curve, const string& id, const MultiFactor& factors, const string& token, const string& message, int epochTime, ClientResult& result);

    private:
        Status ReconstructClientSecret(const Curve& curve, const string& curveName, const string& id, const MultiFactor& factors, const string& token, string& result) const;
        const Curve* GetCurve(const string& name) const;

        Rng m_rng;
        typedef std::unique_ptr<Curve> CurvePtr;
        typedef std::map<string, CurvePtr> CurveMap;
        CurveMap m_curves;
    };
}
