package com.github.fridujo.retrokompat.executabledoc;

import com.github.fridujo.markdown.junit.engine.visitor.MarkdownVisitor;
import com.github.fridujo.markdown.junit.engine.visitor.MarkdownVisitorFactory;

import java.nio.file.Path;

public class ExecutableDocVisitorFactory implements MarkdownVisitorFactory {
    @Override
    public MarkdownVisitor createVisitor(Path path) {
        return new ExecutableDocVisitor();
    }
}
