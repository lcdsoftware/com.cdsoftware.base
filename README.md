# com.cdsoftware.base

- Copyright: 2026 https://www.casadelsoftware.com
- Repository: https://github.com/lcdsoftware/com.cdsoftware.base
- License: GPL 2

## Description

Foundational iDempiere extension for administrative processes and Application Dictionary customizations. It provides IMAP email-to-request processing, PostgreSQL materialized-view refresh, payment reporting data, and deployment of attached home-page templates through Lirionizer. Dictionary packages also extend SQL-expression fields and add legal paper formats.

## Contributors

- 2023–2026 Ángel Lara <angel@casadelsoftware.com>.
- 2025 Carlo Gonzalez <carlogonzalez@casadelsoftware.com>.
- 2025–2026 Eduardo Gil <eduardo@casadelsoftware.com>.

## Components

- iDempiere Plugin [com.cdsoftware.base](com.cdsoftware.base)
- iDempiere Unit Test Fragment [com.cdsoftware.base.test](com.cdsoftware.base.test)

## Prerequisites

- Java 17, commands `java` and `javac`.
- iDempiere 12.
- PostgreSQL for `RefreshMaterializedView`, with a registered materialized view and the necessary database privileges.
- An accessible IMAP mailbox for `RequestEMailProcessor`, with credentials and configured source/destination folders.

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
com.cdsoftware.base.test/src
└── com
    └── cdsoftware
        └── lirionizer
            ├── test
            │   ├── assertion
            │   │   └── Annotations.java
            │   └── util
            │       ├── RandomTestUtil.java
            │       └── ReflectionTestUtil.java
            └── util
                ├── FileTemplateBuilderTest.java
                ├── KeyValueLoggerTest.java
                ├── SqlBuilderTest.java
                └── TimestampUtilTest.java
```

### Processes

Java parameters below must match the installed Application Dictionary. Runtime configuration and required fields must be verified for the selected process.

| Class | Purpose | Main parameters and requirements | Key logic and results |
| --- | --- | --- | --- |
| `Lirionizer` | Deploy attached home-page templates. | `Prefix` must match the attachment names; `jettyPath` is read but currently unused. Requires the process attachments, iDempiere home and writable destination files/directories. | Uses the iDempiere 12 server-plugin layout and build information. Copies HTML/JSP and properties files, sets `TemplatePath`, removes the previous prefix-template directory and extracts the attached ZIP under iDempiere home. It replaces existing resources. |
| `RefreshMaterializedView` | Refresh a PostgreSQL materialized view. | Required `AD_Table_ID`; optional `IsConcurrent` defaults to false. | Resolves an active dictionary table, permits an alphanumeric/underscore identifier, checks `pg_matviews`, and checks for a unique index when concurrent mode is requested. Executes `REFRESH MATERIALIZED VIEW` and reports the mode and elapsed time. PostgreSQL enforces the complete concurrent-refresh and privilege requirements. |
| `RequestEMailProcessor` | Create or update requests from IMAP messages. | Required usable `p_IMAPHost`, `p_IMAPUser`, `p_IMAPPwd` and folder settings `p_InboxFolder`, `p_RequestFolder`, `p_ErrorFolder`. Uses `R_RequestType_ID` or a default request type. Also reads `C_BPartner_ID`, `AD_User_ID`, `AD_Role_ID`, `SalesRep_ID`, `p_DefaultPriority`, `p_DefaultConfidentiality`, `p_NestInbox`, `HTMLAttachmentType`. | Processes messages in date order, checks duplicate Message-ID/sender/sent-date records, resolves senders, handles body/attachments and matches replies by normalized subject. Successful messages are copied to the request folder and deleted/expunged from the inbox. Messages without a sender are copied to the error folder; other exceptions follow the error path and must not be assumed archived successfully. |
| `UnallocatedPayments` | Populate payment reporting data. | Optional `DateAcct` range, `IsReceipt` (`Y`/`N`) and `C_BPartner_ID`. When a start date is supplied, provide a usable end date. | Inserts client payment data from `C_Payment_v` into `T_CDS_C_Payment`, tagged by `AD_PInstance_ID`. Computes `allocatedamttodate` and `openamttodate` from active allocation headers. With a range, both payments and allocations are restricted to that range; this is not a cumulative historical balance as of the end date. The Java query does not restrict results to nonzero open amounts. Database errors are logged and the method returns an empty result. |

### Events, Callouts, Models and Forms

Factories scan their respective annotation packages, but the current plugin source tree contains no concrete event handlers, callouts, custom PO models or forms. `OSGI-INF/*.xml` files register OSGi components; they are not Application Dictionary packages. The test fragment's `resources/xml/xml-invoice.xml` is a test resource.

### Application Dictionary Metadata (2Pack)

Nine packages are included under `com.cdsoftware.base/META-INF`. Package versions represent dictionary updates and differ from the bundle's `12.0.0` version.

| Package | Purpose and dictionary content |
| --- | --- |
| `2Pack_2.0.0.zip` | Base customizations: Lirionizer and IMAP request processes/parameters, theme resources, browser-title configuration, legal paper formats, and text SQL-expression fields in `AD_ViewComponent` and `AD_ChartDatasource`. |
| `2Pack_2.0.1.zip` | Base customizations plus request email sender and Message-ID tracking columns. |
| `2Pack_2.0.2.zip` | Base/request updates including the `CDS_EmailSubject` tracking column. |
| `2Pack_2.0.3.zip` | Updates the `SQLStatement` dictionary column to a text reference, with database synchronization. |
| `2Pack_2.0.4.zip` | Installs payment reporting table `T_CDS_C_Payment` and `UnallocatedPayments` process/report metadata. |
| `2Pack_2.0.5.zip` | Further payment-report dictionary and related process/menu updates. |
| `2Pack_2.0.6.zip` | Combined updates for Lirionizer, email processing, request tracking and payment reporting. |
| `2Pack_2.0.7.zip` | Combined dictionary content including `RefreshMaterializedView` and its parameters. |
| `2Pack_2.0.8.zip` | Latest included dictionary snapshot: all four processes, request email fields, theme/configuration resources and payment reporting metadata. |

All four Java process classes have matching class entries in the included PackOut files. These files being present does not establish that they are installed in a runtime. Some packages include SQL that replaces existing process attachments or resets browser-title configuration; review import results and target settings.

### Configuration and Operational Notes

- **Home-page templates:** Attach `<Prefix>.idempiere.html`, `<Prefix>.idempiere.jsp`, `<Prefix>.home.properties.template` and/or `<Prefix>.template.zip` to the Lirionizer process as needed. An attachment named `home.properties` has a separate destination under the server plugin's home resources. Existing HTML/JSP targets must exist. This version sets `TemplatePath` to `lirionTemplate/`; use a ZIP layout consistent with that path. Runtime build information determines the server-plugin directory.
- **Email processing:** The host supports `imap://host:port` or `imaps://host:port`. The plugin imports the runtime `javax.mail` APIs; verify the bundle resolves those packages. Configure the request type, mailbox folders, sender/default contact and optional role/representative settings. Reply matching is subject-based; it must not be treated as strict Message-ID threading.
- **Materialized views:** Register the view in `AD_Table`. Schema-qualified names are not accepted by this implementation, and catalog checks do not qualify the schema. Verify the database search path and PostgreSQL prerequisites when enabling concurrent mode.
- **Payment reporting:** Consume the generated rows using the current `AD_PInstance_ID`. The implementation does not clean prior rows before inserting. Review server logs if output is empty, and confirm whether the selected range matches the desired business interpretation.

## Instructions

1. Deploy `com.cdsoftware.base` for iDempiere 12 using Java 17 and verify that its OSGi dependencies resolve.
2. Refresh or restart the runtime. The bundle uses `Incremental2PackActivator`; verify the import log for the packaged dictionary updates rather than assuming that importing only the most recent ZIP is sufficient.
3. Review installed process class names, parameters, report tables, role access and affected configuration records.
4. Configure the selected feature using the settings above. For Lirionizer, review the attached resources and the files it will replace. For IMAP, confirm the source/archive folders and request type before enabling scheduled processing.
5. Run each required feature in the target environment and verify its output and logs before operational use.

## Extra Links

- [iDempiere](https://www.idempiere.org/)
- [Casa del Software](https://www.casadelsoftware.com/)
