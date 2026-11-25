package com.liquidglass.calculator.engine

import kotlin.math.pow

enum class FinancialTool(val title: String, val subtitle: String) {
    GST("GST / Tax", "Inclusive & exclusive tax breakdown"),
    EMI("Loan EMI", "Monthly payment, interest & total"),
    DISCOUNT("Discount", "Savings, tax & final sale price"),
    PERCENTAGE("Percentage", "Ratios, % change & markups"),
    TIP_SPLIT("Tip & Split", "Split bills & tips per person"),
    COMPOUND("Compound Interest", "Investment growth over time")
}

data class GstResult(
    val netAmount: Double,
    val gstAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val totalAmount: Double
)

data class EmiResult(
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val principalShare: Double
)

data class DiscountResult(
    val originalPrice: Double,
    val discountAmount: Double,
    val priceAfterDiscount: Double,
    val taxAmount: Double,
    val finalPrice: Double
)

data class TipSplitResult(
    val tipAmount: Double,
    val totalBill: Double,
    val perPerson: Double,
    val tipPerPerson: Double
)

data class CompoundResult(
    val futureValue: Double,
    val totalInvested: Double,
    val interestEarned: Double
)

object FinancialEngine {

    fun calculateGst(amount: Double, ratePercent: Double, isInclusive: Boolean): GstResult {
        if (amount <= 0.0 || ratePercent < 0.0) {
            return GstResult(0.0, 0.0, 0.0, 0.0, 0.0)
        }
        val rate = ratePercent / 100.0
        val net: Double
        val gst: Double
        val total: Double
        if (isInclusive) {
            total = amount
            net = amount / (1.0 + rate)
            gst = total - net
        } else {
            net = amount
            gst = amount * rate
            total = net + gst
        }
        return GstResult(
            netAmount = net,
            gstAmount = gst,
            cgstAmount = gst / 2.0,
            sgstAmount = gst / 2.0,
            totalAmount = total
        )
    }

    fun calculateEmi(principal: Double, annualRatePercent: Double, tenureMonths: Int): EmiResult {
        if (principal <= 0.0 || tenureMonths <= 0) {
            return EmiResult(0.0, 0.0, 0.0, 0.0)
        }
        if (annualRatePercent <= 0.0) {
            val emi = principal / tenureMonths
            return EmiResult(emi, 0.0, principal, 1.0)
        }
        val r = annualRatePercent / 12.0 / 100.0
        val factor = (1.0 + r).pow(tenureMonths.toDouble())
        val emi = principal * r * factor / (factor - 1.0)
        val totalPayment = emi * tenureMonths
        val totalInterest = (totalPayment - principal).coerceAtLeast(0.0)
        val share = if (totalPayment > 0) (principal / totalPayment).coerceIn(0.0, 1.0) else 0.0
        return EmiResult(
            monthlyEmi = emi,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            principalShare = share
        )
    }

    fun calculateDiscount(
        originalPrice: Double,
        discountPercent: Double,
        taxPercent: Double = 0.0
    ): DiscountResult {
        if (originalPrice <= 0.0) {
            return DiscountResult(0.0, 0.0, 0.0, 0.0, 0.0)
        }
        val disc = discountPercent.coerceIn(0.0, 100.0) / 100.0
        val discountAmount = originalPrice * disc
        val afterDiscount = originalPrice - discountAmount
        val taxAmount = afterDiscount * (taxPercent.coerceAtLeast(0.0) / 100.0)
        return DiscountResult(
            originalPrice = originalPrice,
            discountAmount = discountAmount,
            priceAfterDiscount = afterDiscount,
            taxAmount = taxAmount,
            finalPrice = afterDiscount + taxAmount
        )
    }

    fun calculateTipSplit(bill: Double, tipPercent: Double, people: Int): TipSplitResult {
        if (bill <= 0.0 || people <= 0) {
            return TipSplitResult(0.0, 0.0, 0.0, 0.0)
        }
        val tip = bill * (tipPercent.coerceAtLeast(0.0) / 100.0)
        val total = bill + tip
        return TipSplitResult(
            tipAmount = tip,
            totalBill = total,
            perPerson = total / people,
            tipPerPerson = tip / people
        )
    }

    fun calculateCompoundInterest(
        principal: Double,
        monthlyContribution: Double,
        annualRatePercent: Double,
        years: Double,
        compoundsPerYear: Int = 12
    ): CompoundResult {
        if (years <= 0.0 || (principal <= 0.0 && monthlyContribution <= 0.0)) {
            return CompoundResult(0.0, 0.0, 0.0)
        }
        val totalMonths = (years * 12.0).toInt().coerceAtLeast(1)
        val totalInvested = principal + monthlyContribution * totalMonths
        val r = annualRatePercent / 100.0
        if (r == 0.0) {
            return CompoundResult(totalInvested, totalInvested, 0.0)
        }
        val n = compoundsPerYear.coerceAtLeast(1).toDouble()
        val principalFv = principal * (1.0 + r / n).pow(n * years)
        // Monthly contribution effective monthly rate
        val monthlyRate = (1.0 + r / n).pow(n / 12.0) - 1.0
        val contribFv = if (monthlyContribution > 0.0 && monthlyRate > 0.0) {
            monthlyContribution * (((1.0 + monthlyRate).pow(totalMonths.toDouble()) - 1.0) / monthlyRate)
        } else {
            monthlyContribution * totalMonths
        }
        val fv = principalFv + contribFv
        return CompoundResult(
            futureValue = fv,
            totalInvested = totalInvested,
            interestEarned = (fv - totalInvested).coerceAtLeast(0.0)
        )
    }
}
