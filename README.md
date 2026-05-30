# com.cdsoftware.base

- Copyright: 2023 cdsoftware
- Repository: ../
- License: GPL 2

## Description

The `com.cdsoftware.base` plugin is a foundational iDempiere extension containing essential utility and administrative processes. It provides components for managing email-to-request automation (IMAP integration), database materialized view refreshes, unallocated payment extraction for reporting, and a custom theme template deployer (Lirionizer). 

## Contributors

- 2023 Angel Lara <alara@casadelsoftware.com>.

## Components

- iDempiere Plugin [com.cdsoftware.base](com.cdsoftware.base)
- iDempiere Unit Test Fragment [com.cdsoftware.base.test](com.cdsoftware.base.test)

## Prerequisites

- Java 11, commands `java` and `javac`.
- iDempiere 10.0.0
- `javax.mail` package for IMAP processing.

## Features/Documentation

### Source Structure

```text
com.cdsoftware.base/src
└── com
    └── cdsoftware
        └── lirionizer
            ├── base
            │   ├── BundleInfo.java
            │   ├── CustomCallout.java
            │   ├── CustomEvent.java
            │   ├── CustomForm.java
            │   └── CustomProcess.java
            ├── component
            │   ├── CalloutFactory.java
            │   ├── EventFactory.java
            │   ├── FormFactory.java
            │   ├── ModelFactory.java
            │   └── ProcessFactory.java
            ├── process
            │   ├── Lirionizer.java
            │   ├── RefreshMaterializedView.java
            │   ├── RequestEMailProcessor.java
            │   └── UnallocatedPayments.java
            └── util
                ├── FileTemplateBuilder.java
                ├── KeyValueLogger.java
                ├── SqlBuilder.java
                └── TimestampUtil.java
```

| Process | Class | Installed by 2Pack | Annotation | Purpose |
| --- | --- | --- | --- | --- |
| Lirionizer Theme Deployer | `Lirionizer` | Yes | Yes | Extracts and deploys theme template files to the iDempiere and Jetty directories. |
| Refresh Materialized View | `RefreshMaterializedView` | Yes | Yes | Refreshes a PostgreSQL materialized view based on the provided AD_Table_ID. |
| Request EMail Processor | `RequestEMailProcessor` | Yes | Yes | Reads emails from an IMAP server to create or update request records (R_Request). |
| Unallocated Payments | `UnallocatedPayments` | Yes | Yes | Extracts unallocated payment data into a temporary reporting table (`T_CDS_C_Payment`). |

#### Lirionizer
- **Type**: Server Process.
- **Purpose**: Extracts and deploys theme template files to the iDempiere and Jetty directories. It identifies the server configuration and copies HTML, JSP, and ZIP templates to customize the web interface.
- **Main Parameters**: `jettyPath` (Custom path for Jetty server), `Prefix` (Prefix used to identify theme files).
- **Functional Result**: Custom theme templates and ZIP resources are copied to the iDempiere home directory, extracted, and deployed for the web interface.

#### RefreshMaterializedView
- **Type**: Server Process.
- **Purpose**: Refreshes a PostgreSQL materialized view based on the `AD_Table_ID` provided.
- **Main Parameters**: `AD_Table_ID` (ID of the AD_Table representing the materialized view), `IsConcurrent` (Flag to execute concurrently).
- **Key Validations**: Validates `AD_Table_ID` security against SQL injection, ensures the table exists physically as a materialized view, and checks for a unique index if refreshing concurrently.
- **Functional Result**: The specified materialized view is refreshed in PostgreSQL, ensuring up-to-date data for reporting or analysis.

#### RequestEMailProcessor
- **Type**: Server Process.
- **Purpose**: Reads emails from an IMAP server to create or update request records (`R_Request`).
- **Main Parameters**: `p_IMAPHost`, `p_IMAPUser`, `p_IMAPPwd`, `p_InboxFolder`, `p_RequestFolder`, `p_ErrorFolder`, `C_BPartner_ID`, `AD_User_ID`, `R_RequestType_ID`, `p_DefaultPriority`, `p_DefaultConfidentiality`, `HTMLAttachmentType`.
- **Main Logic**: Iterates chronologically over an inbox, maps sender emails to `AD_User` records, identifies email replies to append them to existing requests, handles HTML content and attachments, and routes processed/failed emails to configured IMAP folders.
- **Functional Result**: iDempiere `R_Request` records are automatically populated and updated from emails without manual intervention.

#### UnallocatedPayments
- **Type**: Server Process.
- **Purpose**: Extracts unallocated payment data into a temporary reporting table (`T_CDS_C_Payment`). Calculates allocated and open amounts up to a specified accounting date.
- **Main Parameters**: `DateAcct` (Accounting date range), `IsReceipt` (Filters by receipt or payment), `C_BPartner_ID` (Optional business partner filter).
- **Functional Result**: Populates `T_CDS_C_Payment` with detailed payment data, including computed open amounts, facilitating accurate aging and unallocated payment reports.

## Instructions

- Install the `com.cdsoftware.base` OSGi bundle in your iDempiere environment.
- Import the most recent 2Pack file found in the `META-INF/` folder to install processes and dictionary records.
- For the `RequestEMailProcessor`, configure the IMAP server parameters and ensure the necessary destination IMAP folders (`RequestFolder`, `ErrorFolder`) exist.
- Ensure PostgreSQL user permissions allow executing `REFRESH MATERIALIZED VIEW` for the `RefreshMaterializedView` process.
