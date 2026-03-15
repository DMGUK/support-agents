package com.support.tools;

import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BillingTools {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getPlanInfo(String customerId) {
        return toJson(Map.of(
            "customer_id",   customerId,
            "plan",          "Pro",
            "billing_cycle", "monthly",
            "price_usd",     49.00,
            "next_billing",  LocalDate.now().plusDays(12).toString(),
            "status",        "active"
        ));
    }

    public static String getBillingHistory(String customerId) {
        List<Map<String, Object>> invoices = List.of(
            Map.of("date", LocalDate.now().withDayOfMonth(1).toString(),
                   "amount_usd", 49.00, "status", "paid", "invoice_id", "INV-3041"),
            Map.of("date", LocalDate.now().minusMonths(1).withDayOfMonth(1).toString(),
                   "amount_usd", 49.00, "status", "paid", "invoice_id", "INV-2987"),
            Map.of("date", LocalDate.now().minusMonths(2).withDayOfMonth(1).toString(),
                   "amount_usd", 49.00, "status", "paid", "invoice_id", "INV-2934")
        );
        return toJson(Map.of("customer_id", customerId, "invoices", invoices));
    }

    public static String openRefundRequest(String customerId, String reason) {
        String caseId = "REF-" + (10000 + new Random().nextInt(90000));
        return toJson(Map.of(
            "case_id",     caseId,
            "customer_id", customerId,
            "reason",      reason,
            "status",      "opened",
            "created_at",  LocalDate.now().toString(),
            "message",     "Your refund request has been submitted. " +
                           "You will receive a confirmation email within 1 business day."
        ));
    }

    public static String sendRefundForm(String customerId, String email) {
        return toJson(Map.of(
            "customer_id", customerId,
            "email",       email,
            "sent",        true,
            "message",     "A refund request form has been sent to " + email + ". " +
                           "Please complete it within 7 days."
        ));
    }

    public static String getRefundPolicy() {
        return toJson(Map.of(
            "eligibility",     "Refunds are available within 30 days of charge.",
            "processing_time", "5–10 business days after approval.",
            "partial_refunds", "Pro-rated refunds available for annual plans.",
            "non_refundable",  "One-time setup fees and add-ons are non-refundable.",
            "contact",         "billing@support.example.com"
        ));
    }

    public static String dispatch(String toolName, Map<String, String> args) {
        return switch (toolName) {
            case "get_plan_info"       -> getPlanInfo(args.getOrDefault("customer_id", "unknown"));
            case "get_billing_history" -> getBillingHistory(args.getOrDefault("customer_id", "unknown"));
            case "open_refund_request" -> openRefundRequest(
                                             args.getOrDefault("customer_id", "unknown"),
                                             args.getOrDefault("reason", "not specified"));
            case "send_refund_form"    -> sendRefundForm(
                                             args.getOrDefault("customer_id", "unknown"),
                                             args.getOrDefault("email", "unknown"));
            case "get_refund_policy"   -> getRefundPolicy();
            default                    -> "{\"error\": \"Unknown tool: " + toolName + "\"}";
        };
    }

    private static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\": \"serialization failed\"}";
        }
    }
}
