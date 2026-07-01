# Reference

This section covers each element of the streamlit4j public API across three aspects:
the Java-side signature, the JSON protocol, and the frontend rendering.

The public facade is consolidated in [`io.streamlit4j.core.api.St`](https://github.com/t-izuno/streamlit4j/blob/main/core/src/main/java/io/streamlit4j/core/api/St.java),
and internal work is delegated to package-private classes organized by element category
(`TextWidgets`, `InputWidgets`, and others).

## Category list

| Category | Page | Main elements |
| --- | --- | --- |
| Text & document flow | [text](./text) | title / header / markdown / write / code / latex / html / divider |
| Status & notifications | [status](./status) | metric / toast / progress / spinner / status |
| Tabular data | [data](./data) | dataframe / table / data_editor |
| Media | [media](./media) | image / audio / video |
| Charts | [charts](./charts) | line / bar / area / scatter |
| Input widgets | [inputs](./inputs) | slider / text_input / selectbox / button / date / time / picker, etc. |
| Files | [files](./files) | file_uploader / download_button / download_csv / download_json |
| Layout | [layout](./layout) | columns / container / expander / tabs / sidebar / empty |
| Forms | [forms](./forms) | form / form_submit_button |
| Cache | [cache](./cache) | cacheData / cacheResource |
| Multi-page | [pages](./pages) | pages |
| Custom components | [components](./components) | registerComponent / component |
| Control flow | [control](./control) | rerun / stop / state |

## Additional info

- For the full Javadoc, run `./mvnw -P release package` to generate the `*-javadoc.jar` for each module.
