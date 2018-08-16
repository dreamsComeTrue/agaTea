// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import java.util.Collections

import com.sun.javafx.scene.control.behavior.{BehaviorBase, KeyBinding}
import com.sun.javafx.scene.control.skin.BehaviorSkinBase

class SlideNotificationBarSkin(val control: SlideNotificationBar) extends BehaviorSkinBase[SlideNotificationBar, BehaviorBase[SlideNotificationBar]](control, new BehaviorBase[SlideNotificationBar](control, Collections.emptyList[KeyBinding])) {
}
