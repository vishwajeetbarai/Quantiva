package com.example.datalens.engine

import com.example.datalens.model.ParsedDataset
import java.io.ByteArrayInputStream

object SampleDataGenerator {

    fun getSampleCsvString(): String {
        return buildString {
            append("Date,Product,Category,Region,Customer,Customer State,Quantity,Unit Price,Revenue,Cost,Profit\n")

            // July 2025: Strong revenue (~₹5,10,000 total, with Electronics / Category A leading at ~₹3,20,000)
            append("2025-07-02,UltraBook Pro X,Electronics,North,Tata Enterprises,Maharashtra,12,65000,780000,600000,180000\n")
            append("2025-07-05,Cloud Server Node,Cloud Software,West,Reliance Retail,MH,5,40000,200000,120000,80000\n")
            append("2025-07-08,Ergonomic Chair,Furniture,South,Infosys Tech,Karnataka,20,8500,170000,110000,60000\n")
            append("2025-07-11,Laser Printer 4K,Electronics,East,Wipro Digital,Maharashtra,8,25000,200000,150000,50000\n")
            append("2025-07-14,Desk Organizer Pro,Office Supplies,North,HCL Corp,Delhi,50,1200,60000,35000,25000\n")
            append("2025-07-16,Smart Monitor 32,Electronics,West,Mahindra Tech,Maharastra,15,22000,330000,240000,90000\n")
            append("2025-07-19,Conference Table,Furniture,North,Adani Ports,Gujarat,4,35000,140000,95000,45000\n")
            append("2025-07-22,AI Analytics Suite,Cloud Software,South,Zomato Ltd,Karnataka,10,50000,500000,300000,200000\n")
            append("2025-07-24,Wireless Mouse Pro,Electronics,East,Swiggy Labs,West Bengal,40,1500,60000,36000,24000\n")
            append("2025-07-27,Standing Desk Moto,Furniture,West,Bajaj Auto,Maharashtra,10,18000,180000,120000,60000\n")
            append("2025-07-29,Security Appliance,Electronics,North,Airtel Networks,Delhi,6,75000,450000,340000,110000\n")
            append("2025-07-31,Paper Shredder HD,Office Supplies,South,Titan Company,Tamil Nadu,15,4000,60000,38000,22000\n")

            // Intentional Duplicate Row in July for quality demonstration
            append("2025-07-31,Paper Shredder HD,Office Supplies,South,Titan Company,Tamil Nadu,15,4000,60000,38000,22000\n")

            // August 2025: Dip in Revenue (~₹4,18,000 total, due to supply disruptions in Electronics / Category A dropping 18%)
            append("2025-08-03,UltraBook Pro X,Electronics,North,Tata Enterprises,Maharashtra,5,65000,325000,250000,75000\n")
            append("2025-08-06,Smart Monitor 32,Electronics,West,Godrej Tech,maharashtra,6,22000,132000,96000,36000\n")
            append("2025-08-09,Laser Printer 4K,Electronics,East,Wipro Digital,MH,4,25000,100000,75000,25000\n")
            append("2025-08-12,Ergonomic Chair,Furniture,South,Infosys Tech,Karnataka,18,8500,153000,100000,53000\n")
            append("2025-08-14,Cloud Server Node,Cloud Software,West,Reliance Retail,Maharashtra,8,40000,320000,192000,128000\n")
            append("2025-08-17,Desk Organizer Pro,Office Supplies,North,HCL Corp,Delhi,30,1200,36000,22000,14000\n")
            append("2025-08-19,Conference Table,Furniture,East,ITC Infotech,West Bengal,3,35000,105000,70000,35000\n")
            append("2025-08-22,AI Analytics Suite,Cloud Software,South,PhonePe,Karnataka,7,50000,350000,210000,140000\n")
            append("2025-08-25,Wireless Mouse Pro,Electronics,West,L&T Infotech,Maharashtra,25,1500,37500,22500,15000\n")
            append("2025-08-27,Standing Desk Moto,Furniture,North,Hero Moto,Delhi,8,18000,144000,98000,46000\n")
            // Record with missing Customer State
            append("2025-08-29,Security Appliance,Electronics,North,Paytm Ops,,2,75000,150000,115000,35000\n")
            // Potential Outlier Bulk Order: 220 units!
            append("2025-08-30,Desk Organizer Pro,Office Supplies,West,Govt Directorate,MH,220,1100,242000,150000,92000\n")

            // September 2025: Strong Recovery & Peak (~₹6,40,000)
            append("2025-09-02,UltraBook Pro X,Electronics,North,Tata Enterprises,Maharashtra,14,65000,910000,700000,210000\n")
            append("2025-09-05,Cloud Server Node,Cloud Software,West,Reliance Retail,MH,12,40000,480000,288000,192000\n")
            append("2025-09-07,AI Analytics Suite,Cloud Software,South,Zomato Ltd,Karnataka,15,50000,750000,450000,300000\n")
            append("2025-09-10,Ergonomic Chair,Furniture,South,Infosys Tech,KA,25,8500,212500,135000,77500\n")
            append("2025-09-12,Smart Monitor 32,Electronics,East,Swiggy Labs,West Bengal,18,22000,396000,288000,108000\n")
            append("2025-09-15,Laser Printer 4K,Electronics,North,HCL Corp,Delhi,10,25000,250000,185000,65000\n")
            append("2025-09-18,Conference Table,Furniture,West,Mahindra Tech,Maharashtra,5,35000,175000,115000,60000\n")
            append("2025-09-21,Standing Desk Moto,Furniture,South,Wipro Digital,Karnataka,14,18000,252000,168000,84000\n")
            append("2025-09-24,Security Appliance,Electronics,North,Airtel Networks,Delhi,8,75000,600000,440000,160000\n")
            append("2025-09-26,Wireless Mouse Pro,Electronics,South,Titan Company,Tamil Nadu,60,1500,90000,54000,36000\n")
            // Record with missing Region
            append("2025-09-28,Paper Shredder HD,Office Supplies,,Tech Mahindra,Maharashtra,20,4000,80000,50000,30000\n")
            append("2025-09-30,AI Analytics Suite,Cloud Software,West,Bajaj Finserv,MH,11,50000,550000,330000,220000\n")
        }
    }

    fun createSampleDataset(): ParsedDataset {
        val csv = getSampleCsvString()
        return CsvParser.parse(
            inputStream = ByteArrayInputStream(csv.toByteArray(Charsets.UTF_8)),
            fileName = "Sales_Q3_Retail.csv",
            fileSizeApprox = "8.4 KB"
        )
    }
}
