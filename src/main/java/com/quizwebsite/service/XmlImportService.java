package com.quizwebsite.service;

import com.quizwebsite.model.Answer;
import com.quizwebsite.model.FillBlankQuestion;
import com.quizwebsite.model.MultiAnswerQuestion;
import com.quizwebsite.model.MultiSelectQuestion;
import com.quizwebsite.model.MultipleChoiceQuestion;
import com.quizwebsite.model.PictureResponseQuestion;
import com.quizwebsite.model.Question;
import com.quizwebsite.model.QuestionResponseQuestion;
import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Service
public class XmlImportService {

    private final QuizService quizService;

    public XmlImportService(QuizService quizService) {
        this.quizService = quizService;
    }

    @Transactional
    public Quiz importQuiz(InputStream inputStream, User creator) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            Document document = factory.newDocumentBuilder().parse(inputStream);
            Element root = document.getDocumentElement();

            Quiz quiz = new Quiz();
            quiz.setCreator(creator);
            quiz.setName(attr(root, "title", "Imported quiz"));
            quiz.setDescription(text(root, "description"));
            quiz.setRandomQuestions(Boolean.parseBoolean(root.getAttribute("random")));
            quiz.setMultiPage("multiple".equalsIgnoreCase(root.getAttribute("pageMode")));
            quiz.setImmediateCorrection(Boolean.parseBoolean(root.getAttribute("immediateCorrection")));
            quiz.setPracticeEnabled(true);

            Quiz saved = quizService.create(quiz, root.getAttribute("category"), root.getAttribute("tags"));

            NodeList questionNodes = root.getElementsByTagName("question");
            for (int i = 0; i < questionNodes.getLength(); i++) {
                Element element = (Element) questionNodes.item(i);
                Question question = parseQuestion(element);
                quizService.addQuestion(saved, question);
            }
            return saved;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid quiz XML.", ex);
        }
    }

    private Question parseQuestion(Element element) {
        String type = normalizeType(element.getAttribute("type"));
        Question question = newQuestion(type);
        question.setBody(text(element, "prompt"));
        question.setImageUrl(text(element, "imageUrl"));
        question.setOrdered(Boolean.parseBoolean(element.getAttribute("ordered")));

        if (question instanceof MultipleChoiceQuestion || question instanceof MultiSelectQuestion) {
            Set<String> correct = new HashSet<>();
            NodeList correctNodes = element.getElementsByTagName("correctChoice");
            for (int i = 0; i < correctNodes.getLength(); i++) {
                correct.add(correctNodes.item(i).getTextContent().trim());
            }
            NodeList choices = element.getElementsByTagName("choice");
            for (int i = 0; i < choices.getLength(); i++) {
                String choice = choices.item(i).getTextContent().trim();
                question.addAnswer(new Answer(choice, correct.contains(choice), i));
            }
        } else {
            NodeList answers = element.getElementsByTagName("answer");
            for (int i = 0; i < answers.getLength(); i++) {
                question.addAnswer(new Answer(answers.item(i).getTextContent().trim(), true, i));
            }
        }

        if (question instanceof MultiAnswerQuestion) {
            question.setAnswerSlots(parseInt(element.getAttribute("slots"), question.getAnswers().size()));
        }
        return question;
    }

    private Question newQuestion(String type) {
        return switch (type) {
            case QuestionResponseQuestion.TYPE -> new QuestionResponseQuestion();
            case FillBlankQuestion.TYPE -> new FillBlankQuestion();
            case MultipleChoiceQuestion.TYPE -> new MultipleChoiceQuestion();
            case PictureResponseQuestion.TYPE -> new PictureResponseQuestion();
            case MultiAnswerQuestion.TYPE -> new MultiAnswerQuestion();
            case MultiSelectQuestion.TYPE -> new MultiSelectQuestion();
            default -> throw new IllegalArgumentException("Unknown question type: " + type);
        };
    }

    private String normalizeType(String raw) {
        if ("FILL_IN_BLANK".equalsIgnoreCase(raw)) return FillBlankQuestion.TYPE;
        if ("MULTIPLE_CHOICE_MULTI_ANSWER".equalsIgnoreCase(raw)) return MultiSelectQuestion.TYPE;
        return raw == null || raw.isBlank() ? QuestionResponseQuestion.TYPE : raw.trim().toUpperCase();
    }

    private String attr(Element element, String name, String fallback) {
        String value = element.getAttribute(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String text(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent().trim();
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
