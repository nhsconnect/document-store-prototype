# Path To `prod`

The diagrams below illustrate a segment of our software dev lifecycle from different perspectives. For more details,
please see
the [Definition Of Done](https://gpitbjss.atlassian.net/wiki/spaces/TW/pages/12314869771/Definition+of+done+ARF).

## Story Lifecycle

The diagram below illustrates each stage of a typical story's lifecycle until the story has been moved into the Done
column on the Kanban board.

_Note: All deployment steps (incl. during dev) are dependent pipeline being green at the time._

```mermaid
flowchart LR
    kickOff(Kick-Off) --> inDev(In Dev)
    inDev --> deskCheck(Desk-Check)
    deskCheck -- ACs Not Met -->  inDev
    deskCheck --> qa(QA)
    qa  -- ACs Not Met -->  inDev
    qa --> signOff(Sign-Off)
    signOff --> deployToPreProd(Deploy to pre-prod)
    deployToPreProd --> deployToProd(Deploy to prod)
    deployToProd --> done(Done)
```

## CI

The diagram below illustrates each stage of the CI pipeline including any automatic/manual triggers.

```mermaid
flowchart TB
    subgraph dev
        buildDev(Build) --> planTerraformDev(Plan Terraform)
        buildDev -.-> buildJobsDev
        subgraph buildJobsDev[Build Jobs]
            buildBackendDev(Build Backend)
            buildBackendDev -.- buildAuthoriserDev
            buildAuthoriserDev(Build Authoriser)
        end
        planTerraformDev --> deployTerraformDev(Deploy Terraform)
        deployTerraformDev --> deployUiDev(Deploy UI)
        deployUiDev -.-> deployUiJobsDev
        subgraph deployUiJobsDev[Deploy UI Jobs]
            direction TB
            installDependenciesUiDev(Install Dependencies) --> configureUiDev(Configure)
            configureUiDev --> testUiDev(Test)
            testUiDev --> buildUiDev(Build)
            buildUiDev --> deployUiDevSub(Deploy)
        end
        deployUiDev --> integrationTestsDev(Integration Tests)
        integrationTestsDev --> e2eTestsDev(E2E Tests)
    end
    e2eTestsDev -- Triggers On - Completion --> buildPreProd
    subgraph pre-prod
        buildPreProd(Build) --> planTerraformPreProd(Plan Terraform)
        buildPreProd -.-> buildJobsPreProd
        subgraph buildJobsPreProd[Build Jobs]
            buildBackendPreProd(Build Backend) -.- buildAuthoriserPreProd(Build Authoriser)
        end
        planTerraformPreProd -- Manual Trigger --> deployTerraformPreProd(Deploy Terraform)
        deployTerraformPreProd --> deployUiPreProd(Deploy UI)
        deployUiPreProd -.-> deployUiJobsPreProd
        subgraph deployUiJobsPreProd[Deploy UI Jobs]
            direction TB
            installDependenciesUiPreProd(Install Dependencies) --> configureUiPreProd(Configure)
            configureUiPreProd --> testUiPreProd(Test)
            testUiPreProd --> buildUiPreProd(Build)
            buildUiPreProd --> deployUiPreProdSub(Deploy)
        end
        deployUiPreProd --> integrationTestsPreProd(Integration Tests)
        integrationTestsPreProd --> e2eTestsPreProd(E2E Tests)
    end
    e2eTestsPreProd -- Triggers On - Completion --> buildProd
    subgraph prod
        buildProd(Build) --> planTerraformProd(Plan Terraform)
        buildProd -.-> buildJobsProd
        subgraph buildJobsProd[Build Jobs]
            buildBackendProd(Build Backend) -.- buildAuthoriserProd(Build Authoriser)
        end
        planTerraformProd -- Manual Trigger --> deployTerraformProd(Deploy Terraform)
        deployTerraformProd --> deployUiProd(Deploy UI)
        deployUiProd -.-> deployUiJobsProd
        subgraph deployUiJobsProd[Deploy UI Jobs]
            direction TB
            installDependenciesUiProd(Install Dependencies) --> configureUiProd(Configure)
            configureUiProd --> testUiProd(Test)
            testUiProd --> buildUiProd(Build)
            buildUiProd --> deployUiProdSub(Deploy)
        end
    end
```
