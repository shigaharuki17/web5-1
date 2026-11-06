package com.tfzhang.quiz.utils;

import com.tfzhang.quiz.model.*;
import java.util.List;

public final class Tools {
    private Tools() { }
    private static List<Question> active(List<Question> questions) {
        return questions == null ? List.of() : questions.stream().filter(q -> q != null && Integer.valueOf(0).equals(q.getIsDelete())).toList();
    }
    private static List<AnsBean> answers(Question q) {
        return List.of(new AnsBean(q.getAnswer1Text(), Boolean.TRUE.equals(q.getAnswer1Correct())),
                new AnsBean(q.getAnswer2Text(), Boolean.TRUE.equals(q.getAnswer2Correct())),
                new AnsBean(q.getAnswer3Text(), Boolean.TRUE.equals(q.getAnswer3Correct())),
                new AnsBean(q.getAnswer4Text(), Boolean.TRUE.equals(q.getAnswer4Correct())));
    }
    public static List<QSBeanOut> convertToQSBeanList(List<Question> questions) {
        return active(questions).stream().map(q -> new QSBeanOut(q.getQuestionText(), answers(q))).toList();
    }
    public static List<QSBeanOutManage> convertToQSBeanManageList(List<Question> questions) {
        return active(questions).stream().map(q -> {
            List<AnsBean> answers = answers(q);
            int index = -1;
            for (int i = 0; i < answers.size(); i++) if (answers.get(i).correct()) { index = i; break; }
            return new QSBeanOutManage(q.getId(), q.getQuestionText(), answers.stream().map(AnsBean::text).toList(),
                    index >= 0 ? answers.get(index).text() : "", index >= 0 ? String.valueOf((char)('a' + index)) : "");
        }).toList();
    }
}
