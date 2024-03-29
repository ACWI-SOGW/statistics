# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]


## [1.0.13]
### Changed
- updated to Log4j 2.17.2 (from inherited spring boot 2.10.0)

## [1.0.12]
### Changed
- Updated: rounding rule changed to half_down from half_up and custom rule mechanics removed.
- Added: increased precision management.
- Added: user precision control for values like counts that are exact values. 
- Updated: bypass latest percentile for months with few samples
- Changed latest percentile to be an empty string when the month has too few values rather than null.
- Fixed typo in "percentiles" string to "percentiles"
- Fixed duplicated intermediate values for the current month.

### Notes
- Releases 1.0.2 through 1.0.6 are all pipeline release build testing

## [1.0.1]
### Changed
- Updated pipeline to publish release builds to artifactory releases


## [1.0.0]
### Added
- code.json
- LICENSE.md
- PULL_REQUEST_TEMPLATE.md

### Changed
- Moved inner WLMonthlyStats class to WaterLevelMonthlyStats.java.
- Changed how the latest percentile is calculated.
    - based on monthly medians instead of the entire dataset.
    - remove all data in the current month other than the latest sample.
    - uses latest sample even if provisional properly (previous code attempt to use it).
- fixed a couple swagger, comment typos, and imports.
- BelowLand mediated values in WLSample managed in named variable like AboveDatum.
- renamed and revised useMostPrevalentPCodeMediatedValue to convertToMediatedValue.
- cleaned up test data files: renamed many, added others, and removed non-water-level files.
- created a unit test that shows how mediation changes values while demonstrating forcing BelowLand.
- ensure given samples are sorted by date rather than assuming now that it is an independent service.
- converted Elevation value to BigDecimal
- Latest Percentile is now recored at %100 based vs a decimal value between 0 and 1.
- Overall median is now calculated from monthly medians to avoid giving more weight to months with a higher sampling rate.

## [0.2.0] - 2018-08-30?
### Added
- README.md
- CHANGELOG.md
- Jenkinsfile
- initial commit statistics service see README

### Changed
- refactored statistics class hierarchy from NGWMN

