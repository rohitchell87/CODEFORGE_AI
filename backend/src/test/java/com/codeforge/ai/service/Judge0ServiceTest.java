package com.codeforge.ai.service;

import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class Judge0ServiceTest {

    @Test
    void twoSumWrapperCompiles() throws IOException {
        String source = "class Solution { public int[] twoSum(int[] nums, int target) { return new int[]{0, 1}; } }";
        assertGeneratedCodeCompiles(Judge0Service.buildJavaMainWrapper(source, ""));
    }

    @Test
    void mergeIntervalsWrapperCompiles() throws IOException {
        String source = "class Solution { public int[][] merge(int[][] intervals) { return intervals; } }";
        assertGeneratedCodeCompiles(Judge0Service.buildJavaMainWrapper(source, ""));
    }

    @Test
    void numberOfIslandsWrapperCompiles() throws IOException {
        String source = "class Solution { public int numIslands(char[][] grid) { return grid.length; } }";
        assertGeneratedCodeCompiles(Judge0Service.buildJavaMainWrapper(source, ""));
    }

    @Test
    void validParenthesesWrapperCompiles() throws IOException {
        String source = "class Solution { public boolean isValid(String s) { return s != null; } }";
        assertGeneratedCodeCompiles(Judge0Service.buildJavaMainWrapper(source, ""));
    }

    private static void assertGeneratedCodeCompiles(String sourceCode) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Java compiler is not available in this environment");

        Path tempDir = Files.createTempDirectory("judge0-wrapper-test");
        Path javaFile = tempDir.resolve("Main.java");
        Files.writeString(javaFile, sourceCode);

        int result = compiler.run(null, null, null, javaFile.toString());
        assertEquals(0, result, "Generated Main.java should compile");
    }
}
