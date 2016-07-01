*This is an open source library. Please do not create branches, public forks or pull requests containing sensitive information*

# Float

An Android library by for displaying instructional overlays or 'coach marks' that _float_ above the current window.

Coach marks can be associated with a particular view but are independent of the underlying layout, making it easy to add them to new and existing projects alike. Separation from the standard layout also makes it easier to control where and when overlays are visible, which is particularly useful when coach marks are temporary or context specific.

There are currently 4 types of pre-built coach mark: [BubbleCoachMark](#bubble), [HighlightCoachMark](#highlight), [LayeredCoachMark](#layered) and [PunchHoleCoachMark](#punchhole).

### <a name="bubble">BubbleCoachMark</a>

A speech bubble that can be configured to point to a particular view, or sub-region within a view. Bubble coach marks can include a simple message or a fully customisable view.

### <a name="highlight">HighlightCoachMark</a>

A thin border around a particular view, or sub-region.

### <a name="layered">LayeredCoachMark</a>

A translucent layer onto the particular view, or sub-region. Layered coach marks can include a simple message or a fully customisable view.

### <a name="punchhole">PunchHoleCoachMark</a>

A translucent layer onto the particular view and "Punch Hole" for given child view component. The target child view should be within the parent view to properly located the hole. PunchHole coach marks can include a simple message or a fully customisable view where upper or lower side of the hole. 

### Building and running the tests

```
./gradlew clean build test connectedAndroidTest
```

### Proguard rules

It's safe to use either `getDefaultProguardFile('proguard-android.txt')` or `getDefaultProguardFile('proguard-android-optimize.txt')`. CornedBeef uses the Android animator APIs which require reflection and can make some methods appear to be deadcode to proguard.

Use the following explicit rule to make sure these APIs remain if you are experiencing problems with the PunchHoleCoachMark animations:

```
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}
```

## Code of conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments

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

-------

Created by [SwiftKey](https://www.swiftkey.com/)

