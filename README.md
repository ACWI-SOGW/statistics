# Statistics
A time series statistics micro service. It accepts time series data and returns overall and monthly statistics for months that qualify.

Overall statistics returned include:
```Highest Water Level	
Median Water Level	
Lowest Water Level	
First Measurement Date	
Last Measurement Date	
Years of Record
Number of Measurements	
```
Monthly statistics are returned only of they qualify. Each month must have ten unique years of data and latest sample must be within 406 days from submission date. The returns statistics include:
```10th Percentile	
25th Percentile	
50th Percentile	
75th Percentile	
90th Percentile	
Lowest Median	
Highest Median	
Years of Record
Number of Measurements for the month
```
Sample JSON respones
```
{
  "overall": {
    "MEDIATION":"BelowLand",
    "MAX_VALUE":"1.000",
    "MEDIAN":"1.500",
    "MIN_VALUE":"43.000",
    "MIN_DATE":"2005-06-10T04:15:00-05:00",
    "MAX_DATE":"2018-06-10T04:15:00-05:00",
    "RECORD_YEARS":"13.0",
    "LATEST_VALUE":"11.000",
    "SAMPLE_COUNT":"28",
    "LATEST_PCTILE":"0.31250"
    "CALC_DATE":"2018-08-09",
    "IS_RANKED":"Y",
  },
  "monthly": {
    "6": {
    "P10":"32.500",
    "P25":"13.250",
    "P50":"1.500",
    "P75":"1.000"
    "P90":"1.000",
    "P50_MIN":"43.000",
    "P50_MAX":"1.000",
    "RECORD_YEARS":"14",
    "SAMPLE_COUNT":"15",
    }, "7": {
    "P10":"34.600",
    "P25":"15.000",
    "P50":"1.000",
    "P75":"1.000"
    "P90":"1.000",
    "P50_MIN":"43.000",
    "P50_MAX":"1.000",
    "RECORD_YEARS":"13",
    "SAMPLE_COUNT":"13",
    }
  },
}
```
