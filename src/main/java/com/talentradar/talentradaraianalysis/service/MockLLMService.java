package com.talentradar.talentradaraianalysis.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockLLMService {
    public ReflectionAnalysisResult analyzeReflection(String reflection) {
        if (reflection == null || reflection.isBlank()) {
            return new ReflectionAnalysisResult("neutral", List.of(), List.of());
        }
        String lower = reflection.toLowerCase();
        List<String> keywords = List.of("teamwork", "learning", "delivery", "communication", "mentor", "lead", "growth", "collaboration");
        List<String> found = new ArrayList<>();
        for (String k : keywords) {
            if (lower.contains(k)) found.add(k);
        }
        // Tone analysis (very simple)
        String tone = "neutral";
        if (lower.contains("proud") || lower.contains("happy") || lower.contains("excited") || lower.contains("improved") || lower.contains("success")) {
            tone = "positive";
        } else if (lower.contains("struggle") || lower.contains("difficult") || lower.contains("problem") || lower.contains("fail")) {
            tone = "negative";
        }
        // Extract goals/actions (look for 'goal', 'plan', 'aim', 'next')
        List<String> goals = new ArrayList<>();
        String[] sentences = reflection.split("[.!?]\s*");
        for (String s : sentences) {
            String sLower = s.toLowerCase();
            if (sLower.contains("goal") || sLower.contains("plan") || sLower.contains("aim") || sLower.contains("next")) {
                goals.add(s.trim());
            }
        }
        return new ReflectionAnalysisResult(tone, found, goals);
    }

    public String generateOverallFeedback(List<String> strengths, List<String> improvementAreas, String reflection, ReflectionAnalysisResult reflectionResult) {
        StringBuilder feedback = new StringBuilder();
        if (!strengths.isEmpty()) {
            feedback.append("You’re performing well in ").append(String.join(", ", strengths)).append(". ");
        }
        if (!improvementAreas.isEmpty()) {
            feedback.append("Consider focusing on improving ").append(String.join(", ", improvementAreas)).append(". ");
        }
        if (reflection != null && !reflection.isBlank()) {
            if (!reflectionResult.keywordsFound.isEmpty()) {
                feedback.append("Your reflection highlights: ").append(String.join(", ", reflectionResult.keywordsFound)).append(". ");
            }
            if (!reflectionResult.goals.isEmpty()) {
                feedback.append("You have set goals such as: ").append(String.join("; ", reflectionResult.goals)).append(". ");
            }
            feedback.append("The overall tone of your reflection is ").append(reflectionResult.tone).append(". ");
        } else {
            feedback.append("Providing a reflection can offer deeper insights into your progress.");
        }
        return feedback.toString().trim();
    }

    public static class ReflectionAnalysisResult {
        public final String tone;
        public final List<String> keywordsFound;
        public final List<String> goals;
        public ReflectionAnalysisResult(String tone, List<String> keywordsFound, List<String> goals) {
            this.tone = tone;
            this.keywordsFound = keywordsFound;
            this.goals = goals;
        }
    }
} 