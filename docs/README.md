# Docs

This directory contains docs on the [tech](tech) involved with this codebase as well as [diagrams](diagrams) to
illustrate the former and more.

## Mermaid

[Mermaid](https://mermaid.js.org/) is a JS-based diagramming and charting tool that renders Markdown-inspired text
definitions to create and modify
diagrams dynamically.

### Diagram Creation

To create a new Mermaid diagram, create a new `.md` file in the appropriate directory within [diagrams](diagrams) and
wrap your diagram in a code
block specified with `mermaid`. You can use [Mermaid Live Editor](https://mermaid.live/edit) to help the diagrams and
the code to go inside the aforementioned code block in the Markdown.

### IntelliJ Setup

To visualise Mermaid diagrams in IntelliJ's Markdown preview, you need to enable it by:

1. Go to IntelliJ's `Preferences...`
2. Click `Languages & Frameworks`
3. Click `Markdown`
4. Under `Markdown Extensions:` and next to `Mermaid`, click `Install`
5. Check the box next to `Mermaid`
6. Click `Apply` and `OK`
