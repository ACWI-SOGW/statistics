# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
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


## [0.2.0] - 2018-08-30?
### Added
- README.md
- CHANGELOG.md
- Jenkinsfile
- initial commit statistics service see README

### Changed
- refactored statistics class hierarchy from NGWMN

