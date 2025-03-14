package com.github.gabrielortiz00.byp_costing_jdeploy;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ResultsModel implements Serializable {

    private String projectName;
    private String productName;
    private LocalDateTime createdAt;

    //input parameters
    private double unitCost; // C: unit cost in USD
    private double incrementalCost; // I: incremental per-unit costs in USD
    private double shippingCost; // L: total shipping cost for one container in USD
    private int unitsPerContainer; // Q: number of units that fit into one container
    private double taxRate; // T: import tax rate (decimal)
    private double exchangeRate; // R: exchange rate (MXN per USD)
    private double exchangeBuffer; // B: exchange rate buffer (decimal)

    //output / input parameter depending on selected type of calculation
    private double sellingPrice; // P: selling price in MXN
    private double profitMargin; // M: desired profit margin (decimal)

    //results variables
    private double containerCostUsd;
    private double containerCostWithTax;
    private double containerCostMxn;
    private double costPerUnitMxn;

    //flag for tracking selected calculation type
    private boolean calculatedFromMargin;

    //constructor from margin to price
    public ResultsModel(String projectName, String productName,
                        double unitCost, double incrementalCost,
                        double shippingCost, int unitsPerContainer,
                        double taxRate, double exchangeRate,
                        double exchangeBuffer, double profitMargin) {

        this.projectName = projectName;
        this.productName = productName;
        this.createdAt = LocalDateTime.now();
        this.unitCost = unitCost;
        this.incrementalCost = incrementalCost;
        this.shippingCost = shippingCost;
        this.unitsPerContainer = unitsPerContainer;
        this.taxRate = taxRate;
        this.exchangeRate = exchangeRate;
        this.exchangeBuffer = exchangeBuffer;
        this.profitMargin = profitMargin;
        this.calculatedFromMargin = true;
        calculatePriceFromMargin();
    }

    //constructor from price to margin
    public ResultsModel(String projectName, String productName,
                        double unitCost, double incrementalCost,
                        double shippingCost, int unitsPerContainer,
                        double taxRate, double exchangeRate,
                        double exchangeBuffer, double sellingPrice,
                        boolean isPriceInput) {

        this.projectName = projectName;
        this.productName = productName;
        this.createdAt = LocalDateTime.now();
        this.unitCost = unitCost;
        this.incrementalCost = incrementalCost;
        this.shippingCost = shippingCost;
        this.unitsPerContainer = unitsPerContainer;
        this.taxRate = taxRate;
        this.exchangeRate = exchangeRate;
        this.exchangeBuffer = exchangeBuffer;
        this.sellingPrice = sellingPrice;
        this.calculatedFromMargin = false;
        calculateMarginFromPrice();
    }

    //common costs independent of chosen calculation type
    private void calculateCommonCosts() {
        //containerCostUsd = ((C + I) * Q) + L
        containerCostUsd = (unitCost + incrementalCost) * unitsPerContainer + shippingCost;

        //containerCostWithTax = containerCostUsd * (1 + T)
        containerCostWithTax = containerCostUsd * (1 + taxRate);

        //containerCostMxn = containerCostWithTax * R * (1 + B)
        containerCostMxn = containerCostWithTax * exchangeRate * (1 + exchangeBuffer);

        //costPerUnitMxn = containerCostMxn / Q
        costPerUnitMxn = containerCostMxn / unitsPerContainer;
    }

    /**
     * calculate selling price based on margin
     * formula: P = C / (1 - M)
     * where P is selling price, C is cost, and M is margin
     */
    private void calculatePriceFromMargin() {
        calculateCommonCosts();

        //sellingPrice = costPerUnitMxn / (1 - M)
        sellingPrice = costPerUnitMxn / (1 - profitMargin);
    }

    /**
     * calculate profit margin based on price
     * formula: M = (P - C) / P
     * where P is selling price, C is cost, and M is margin
     */
    private void calculateMarginFromPrice() {
        calculateCommonCosts();

        //profitMargin = (P - costPerUnitMxn) / P
        profitMargin = (sellingPrice - costPerUnitMxn) / sellingPrice;
    }

    //getter methods
    public String getProjectName() {
        return projectName;
    }

    public String getProductName() {
        return productName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public double getIncrementalCost() {
        return incrementalCost;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public int getUnitsPerContainer() {
        return unitsPerContainer;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public double getExchangeBuffer() {
        return exchangeBuffer;
    }

    public double getContainerCostUsd() {
        return containerCostUsd;
    }

    public double getContainerCostWithTax() {
        return containerCostWithTax;
    }

    public double getContainerCostMxn() {
        return containerCostMxn;
    }

    public double getCostPerUnitMxn() {
        return costPerUnitMxn;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public double getProfitMargin() {
        return profitMargin;
    }

    public boolean isCalculatedFromMargin() {
        return calculatedFromMargin;
    }
}
