package com.github.koolskateguy89.mobileos.utils

import com.google.common.base.Strings
import javafx.animation.FadeTransition
import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextField
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.util.Duration
import org.controlsfx.control.textfield.CustomPasswordField
import org.controlsfx.control.textfield.CustomTextField
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory

object Utils {

    @JvmStatic
    fun initRootDir(file: File) {
        val root: Path = file.toPath()

        val apps = root.resolve(Constants.APPS_DIR)
        if (!apps.isDirectory())
            apps.createDirectories()

        val sysApps = root.resolve(Constants.SYS_APPS_DIR)
        if (!sysApps.isDirectory())
            sysApps.createDirectories()
    }


    // recursive copy folder: https://stackoverflow.com/a/60621544
    @JvmStatic
    fun copyFolder(source: Path, target: Path, vararg options: CopyOption) {
        Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.createDirectories(target.resolve(source.relativize(dir)))
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.copy(file, target.resolve(source.relativize(file)), *options)
                return FileVisitResult.CONTINUE
            }
        })
    }

    // recursively delete folder: https://stackoverflow.com/a/27917071
    @JvmStatic
    fun deleteDirectory(directory: Path) {
        clearDirectory(directory)
        Files.delete(directory)
    }

    @JvmStatic
    fun clearDirectory(directory: Path) {
        Files.walkFileTree(directory, FileDeleter())
    }

    open class FileDeleter : SimpleFileVisitor<Path>() {
        @Throws(IOException::class)
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            Files.delete(file)
            return FileVisitResult.CONTINUE
        }

        @Throws(IOException::class)
        override fun postVisitDirectory(dir: Path, exc: IOException) = FileVisitResult.CONTINUE
    }


    @JvmStatic
    fun String.copyToClipboard() {
        val content = ClipboardContent()
        content.putString(this)
        Clipboard.getSystemClipboard().setContent(content)
    }


    @JvmStatic
    fun Node.anchor(top: Double, bottom: Double, left: Double, right: Double) {
        AnchorPane.setTopAnchor(this, top)
        AnchorPane.setBottomAnchor(this, bottom)
        AnchorPane.setLeftAnchor(this, left)
        AnchorPane.setRightAnchor(this, right)
    }

    @JvmStatic
    fun CustomTextField.makeClearable() {
        setupClearButtonField(this, rightProperty())
    }

    @JvmStatic
    fun CustomPasswordField.makeClearable() {
        setupClearButtonField(this, rightProperty())
    }

    // straight up copied from org.controlsfx.control.textfield.TextFields.createClearableTextField,
    // adapted a bit to Kotlin
    // why don't they just make it public :/
    private val FADE_DURATION = Duration.millis(350.0)

    @JvmStatic
    fun setupClearButtonField(inputField: TextField, rightProperty: ObjectProperty<Node?>) {
        inputField.styleClass.add("clearable-field") //$NON-NLS-1$

        val clearButton = Region()
        clearButton.styleClass.addAll("graphic") //$NON-NLS-1$
        val clearButtonPane = StackPane(clearButton)
        clearButtonPane.styleClass.addAll("clear-button") //$NON-NLS-1$
        clearButtonPane.opacity = 0.0
        clearButtonPane.cursor = Cursor.DEFAULT
        clearButtonPane.onMouseReleased = EventHandler { inputField.clear() }
        clearButtonPane.managedProperty().bind(inputField.editableProperty())
        clearButtonPane.visibleProperty().bind(inputField.editableProperty())

        rightProperty.set(clearButtonPane)

        val fader = FadeTransition(FADE_DURATION, clearButtonPane)
        fader.cycleCount = 1

        fun setButtonVisible(visible: Boolean) {
            fader.fromValue = if (visible) 0.0 else 1.0
            fader.toValue = if (visible) 1.0 else 0.0
            fader.play()
        }

        inputField.textProperty().addListener(InvalidationListener {
            val text = inputField.text
            val isTextEmpty = Strings.isNullOrEmpty(text)
            val isButtonVisible = fader.node.opacity > 0
            if (isTextEmpty && isButtonVisible) {
                setButtonVisible(false)
            } else if (!isTextEmpty && !isButtonVisible) {
                setButtonVisible(true)
            }
        })
    }


    @JvmStatic
    fun String.isNaturalNumber(): Boolean = all(Character::isDigit)

    @JvmStatic
    fun onlyAllowNaturalNumbersListener() = ChangeListener<String> { obs, oldVal, newVal ->
        if (!newVal.isNullOrEmpty() && !newVal.isNaturalNumber())
            (obs as StringProperty).value = oldVal
    }


    // String contains ignore case: https://stackoverflow.com/a/25379180
    @JvmStatic
    fun containsIgnoreCase(src: String, what: String) = src.contains(what, ignoreCase = true)

    @JvmStatic
    fun <T> nonNullElse(obj: T?, defaultObj: T): T = obj ?: defaultObj


    @JvmStatic
    fun ask(question: String): String = CustomTextField().apply { makeClearable() }.let {
        Alert(Alert.AlertType.INFORMATION).apply {
            title = question
            headerText = ""
            buttonTypes.setAll(ButtonType.OK)

            dialogPane.content = it

            onShown = EventHandler { _ -> it.requestFocus() }
        }.showAndWait()

        it.text
    }

    @JvmStatic
    fun isNullOrBlank(s: String?) = s.isNullOrBlank()

}
