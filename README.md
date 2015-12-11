# CornedBeef

An Android library by for displaying instructional overlays or 'coach marks' that _float_ above the current window.

Coach marks can be associated with a particular view but are independent of the underlying layout, making it easy to add them to new and existing projects alike. Separation from the standard layout also makes it easier to control where and when overlays are visible, which is particularly useful when coach marks are temporary or context specific.

There are currently two types of pre-built coach mark: [BubbleCoachMark](#bubble) and [HighlightCoachMark](#highlight).

### <a name="bubble">BubbleCoachMark</a>

A speech bubble that can be configured to point to a particular view, or sub-region within a view. Bubble coach marks can include a simple message or a fully customisable view.

### <a name="highlight">HighlightCoachMark</a>

A thin border around a particular view, or sub-region.

### Building and running the tests

```
./gradlew clean build test connectedAndroidTest
```

## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--------

Created by [SwiftKey](https://www.swiftkey.com/)
