package com.jinatra.finatra.util

import kotlin.math.pow

/**
 * Equated Monthly Installment (EMI) math for loan tracking (PRD 6.x EMI plan).
 *
 * Standard amortization: a fixed monthly payment covers interest on the outstanding balance plus
 * a growing slice of principal. A zero-rate loan is just principal split evenly across the tenure.
 */
object Emi {

    /** Snapshot of where a loan stands today: the fixed payment, how many of [tenureMonths] have
     *  elapsed, the outstanding balance, and the total interest over the full loan. */
    data class Schedule(
        val monthlyPayment: Double,
        val tenureMonths: Int,
        val monthsPaid: Int,
        val remainingBalance: Double,
        val totalInterest: Double,
    ) {
        /** 0..1 share of the tenure completed (by payments made). */
        val fractionPaid: Float get() = if (tenureMonths > 0) (monthsPaid.toFloat() / tenureMonths).coerceIn(0f, 1f) else 0f
        val isPaidOff: Boolean get() = monthsPaid >= tenureMonths
    }

    /** Fixed monthly payment for [principal] over [tenureMonths] at [annualRatePct] (annual %). */
    fun monthlyPayment(principal: Double, annualRatePct: Double, tenureMonths: Int): Double {
        if (principal <= 0.0 || tenureMonths <= 0) return 0.0
        val r = annualRatePct / 12.0 / 100.0
        if (r == 0.0) return principal / tenureMonths
        val pow = (1 + r).pow(tenureMonths)
        return principal * r * pow / (pow - 1)
    }

    /** Outstanding balance after [monthsPaid] payments (clamped to the tenure). */
    fun remainingBalance(principal: Double, annualRatePct: Double, tenureMonths: Int, monthsPaid: Int): Double {
        if (principal <= 0.0 || tenureMonths <= 0) return 0.0
        val k = monthsPaid.coerceIn(0, tenureMonths)
        if (k >= tenureMonths) return 0.0
        val r = annualRatePct / 12.0 / 100.0
        val pay = monthlyPayment(principal, annualRatePct, tenureMonths)
        if (r == 0.0) return (principal - pay * k).coerceAtLeast(0.0)
        val pow = (1 + r).pow(k)
        return (principal * pow - pay * (pow - 1) / r).coerceAtLeast(0.0)
    }

    /** Build a full [Schedule] for a loan given how many months have been paid so far. */
    fun schedule(principal: Double, annualRatePct: Double, tenureMonths: Int, monthsPaid: Int): Schedule {
        val pay = monthlyPayment(principal, annualRatePct, tenureMonths)
        val k = monthsPaid.coerceIn(0, tenureMonths)
        return Schedule(
            monthlyPayment = pay,
            tenureMonths = tenureMonths,
            monthsPaid = k,
            remainingBalance = remainingBalance(principal, annualRatePct, tenureMonths, k),
            totalInterest = (pay * tenureMonths - principal).coerceAtLeast(0.0),
        )
    }
}
