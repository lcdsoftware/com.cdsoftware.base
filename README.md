# com.cdsoftware.base
- Copyright: 2026 https://www.casadelsoftware.com
- Repository: https://bitbucket.org/cdsoftware/com.cdsoftware.base.git
- License: GPL 2

## Description
The `com.cdsoftware.base` plugin is a custom extension for iDempiere. It extends standard system capabilities by providing custom server processes, and Application Dictionary configurations (2Pack) to support customized business workflows.

## Contributors
- 2023 Angel Lara <alara@casadelsoftware.com>.

## Components
- iDempiere Plugin [com.cdsoftware.base](com.cdsoftware.base)
- iDempiere Unit Test Fragment [com.cdsoftware.base.test](com.cdsoftware.base.test)

## Prerequisites
- Java 11, commands `java` and `javac`.
- iDempiere 11

## Features/Documentation
### Source Structure
```
├── com/
        ├── cdsoftware/
            ├── lirionizer/
                ├── util/
                    ├── FileTemplateBuilder.java
                    ├── KeyValueLogger.java
                    ├── SqlBuilder.java
                    ├── TimestampUtil.java
                ├── base/
                    ├── BundleInfo.java
                    ├── CustomCallout.java
                    ├── CustomEvent.java
                    ├── CustomForm.java
                    ├── CustomProcess.java
                ├── component/
                    ├── CalloutFactory.java
                    ├── EventFactory.java
                    ├── FormFactory.java
                    ├── ModelFactory.java
                    ├── ProcessFactory.java
                ├── process/
                    ├── Lirionizer.java
                    ├── RefreshMaterializedView.java
                    ├── RequestEMailProcessor.java
                    ├── UnallocatedPayments.java
```

### Processes

| Class Name | Purpose | Main Parameters | Key Logic & Results |
| --- | --- | --- | --- |
| `UnallocatedPayments` | Server process. | `C_BPartner_ID`, `DateAcct`, `IsReceipt` | Executes core logic and updates database records. |
| `RefreshMaterializedView` | Server process. | `AD_Table_ID`, `IsConcurrent` | Executes core logic and updates database records. |
| `Lirionizer` | Server process. | None | Executes core logic and updates database records. |
| `RequestEMailProcessor` | Server process. | `AD_Role_ID`, `AD_User_ID`, `C_BPartner_ID`, `HTMLAttachmentType`, `R_RequestType_ID`, `SalesRep_ID` | Updates approval status and logs the approver's user ID. |





### Application Dictionary Metadata (2Pack)

| Package / File Name | Purpose & Dictionary Configurations |
| --- | --- |
| `2Pack_1.0.10.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_1.0.3.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_1.0.4.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_1.0.5_ExpandColumnSize.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_1.0.6_AddParameter.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_1.0.7.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_1.0.8.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_1.0.9.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.0.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.1.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.2.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.3.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.4.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.5.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.6.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.7.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `2Pack_2.0.8.zip` | Metadata package containing Application Dictionary (AD) configurations. |
| `CalloutFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `EventFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `FormFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `ModelFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `ProcessFactory.xml` | Metadata package containing Application Dictionary (AD) configurations. |
| `xml-invoice.xml` | Metadata package containing Application Dictionary (AD) configurations. |


## Instructions
1. Deploy the `com.cdsoftware.base` OSGi bundle in your iDempiere environment.
2. Restart iDempiere and refresh OSGi bundles to register factories.
3. Configure dictionary and role access rules as needed.