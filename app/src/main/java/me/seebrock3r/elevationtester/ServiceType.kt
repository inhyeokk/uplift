package me.seebrock3r.elevationtester

enum class ServiceType(val elevation: Int, val ambient: Float, val spot: Float) {
    A(20, 0.1f, 0.03f),
    B(8, 0.03f, 0.05f),
    C(7, 0f, 1f),
    ;
}