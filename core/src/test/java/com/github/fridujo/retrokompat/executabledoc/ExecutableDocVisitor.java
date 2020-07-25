package com.github.fridujo.retrokompat.executabledoc;

import com.github.fridujo.markdown.junit.engine.visitor.ContainerNode;
import com.github.fridujo.markdown.junit.engine.visitor.MarkdownVisitor;
import com.github.fridujo.markdown.junit.engine.visitor.RunnableNode;
import com.github.fridujo.markdown.junit.engine.visitor.TestNode;
import org.commonmark.node.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.stream.Collectors;

class ExecutableDocVisitor extends AbstractVisitor implements MarkdownVisitor {

    private final Deque<PathElement> nodePath = new ArrayDeque<>();
    private final Collection<TestNode.Builder> testNodes = new ArrayList<>();
    private int codeBlockSequence = 0;

    private RunnableTestCase currentTestCase;

    @Override
    public void visit(Heading heading) {
        codeBlockSequence = 0;
        currentTestCase = new RunnableTestCase();
        int level = heading.getLevel();
        String headingText = ((Text) heading.getFirstChild()).getLiteral();
        if (level > previousHeadingLevel()) {
            addNewContainer(level, headingText);
        } else {
            while (level <= previousHeadingLevel()) {
                nodePath.pop();
            }
            addNewContainer(level, headingText);
        }
    }

    @Override
    public void visit(Text text) {
        String literal = text.getLiteral().toLowerCase();
        if (literal.contains("not compatible")) {
            currentTestCase.compatible = false;
        } else if (literal.contains("compatible")) {
            currentTestCase.compatible = true;
        }
        if (literal.contains("new version")) {
            currentTestCase.v2CodeFragment = true;
        } else if (literal.contains("previous version")) {
            currentTestCase.v2CodeFragment = false;
        }

        if (literal.contains("error") && text.getNext() instanceof Code) {
            currentTestCase.errorMessages.add(((Code) text.getNext()).getLiteral());
        }
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        if (!"java".equals(fencedCodeBlock.getInfo())) {
            return;
        } else if (nodePath.peekFirst() == null) {
            throw new IllegalStateException("Java code block found outside a heading context:\n\n" + fencedCodeBlock.getLiteral().trim());
        }
        boolean isLeafContainer = nodePath.peekFirst().node.type() == TestNode.Type.CONTAINER;
        if (isLeafContainer) {
            PathElement leafContainer = nodePath.pop();
            RunnableNode.Builder runnableBuilder = new RunnableNode.Builder(leafContainer.node.name(), currentTestCase);
            PathElement parent = nodePath.peekFirst();
            if (parent == null) {
                testNodes.add(runnableBuilder);
            } else {
                ContainerNode.Builder.class.cast(parent.node).addChild(runnableBuilder);
            }
            nodePath.push(new PathElement(leafContainer.level, runnableBuilder));
        }
        currentTestCase.addSource(fencedCodeBlock.getLiteral().trim());
    }

    private void addNewContainer(int level, String headingText) {
        var newContainer = new ContainerNode.Builder(headingText);
        PathElement parentNode = nodePath.peekFirst();
        if (parentNode == null) {
            testNodes.add(newContainer);
        } else {
            if (parentNode.node().type() == TestNode.Type.CONTAINER) {
                ContainerNode.Builder.class.cast(parentNode.node()).addChild(newContainer);
            } else {
                throw new IllegalStateException("Can not append a node to the non-container " + parentNode);
            }
        }
        nodePath.push(new PathElement(level, newContainer));
    }

    private int previousHeadingLevel() {
        PathElement parentNode = nodePath.peekFirst();
        return parentNode != null ? parentNode.level : 0;
    }

    @Override
    public Collection<TestNode> getCollectedTestNode() {
        return testNodes.stream().map(TestNode.Builder::build).collect(Collectors.toList());
    }

    private record PathElement(int level, TestNode.Builder node) {
    }
}
