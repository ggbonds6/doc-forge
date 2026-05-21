package com.docforge.govdoc;

import com.docforge.govdoc.common.ApiException;
import com.docforge.govdoc.template.TemplateAcceptanceCriterion;
import com.docforge.govdoc.template.TemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TemplateAcceptanceWorkflowTest {
    @Autowired
    private TemplateService templateService;

    @Test
    void shouldBlockPublishingUntilBlockingAcceptancePassed() {
        TemplateService.TemplateVersionDetail draft = createNoticeDraft("验收发布测试模板");
        String draftId = draft.id();

        assertThatThrownBy(() -> templateService.publish(draftId, new TemplateService.PublishTemplateRequest(null, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("模板验收未通过");

        for (TemplateAcceptanceCriterion criterion : draft.previewBaseline().getCriteria()) {
            if (criterion.isBlocking()) {
                draft = templateService.reviewAcceptance(
                        draft.id(),
                        new TemplateService.AcceptanceReviewRequest(criterion.getCode(), "PASS", "测试验收员", "WPS 验收通过")
                );
            }
        }

        assertThat(draft.previewBaseline().getReviewStatus()).isEqualTo("ACCEPTED");
        TemplateService.TemplateVersionDetail published = templateService.publish(draft.id(), new TemplateService.PublishTemplateRequest(null, null));
        assertThat(published.status()).isEqualTo("PUBLISHED");
        assertThat(published.previewBaseline().getReviewStatus()).isEqualTo("PUBLISHED_ACCEPTED");
    }

    @Test
    void shouldAllowOverridePublishingWithReason() {
        TemplateService.TemplateVersionDetail draft = createNoticeDraft("强制发布测试模板");

        TemplateService.TemplateVersionDetail published = templateService.publish(
                draft.id(),
                new TemplateService.PublishTemplateRequest("线下已完成人工验收，线上记录稍后补齐", "测试管理员")
        );

        assertThat(published.status()).isEqualTo("PUBLISHED");
        assertThat(published.previewBaseline().getReviewStatus()).isEqualTo("OVERRIDE_PUBLISHED");
        assertThat(published.previewBaseline().getPublishOverrideReason()).contains("线下已完成人工验收");
    }

    private TemplateService.TemplateVersionDetail createNoticeDraft(String prefix) {
        String sourceTemplateVersionId = templateService.listTemplatePacks().stream()
                .filter(pack -> "NOTICE".equals(pack.code()))
                .flatMap(pack -> pack.versions().stream())
                .filter(version -> "PUBLISHED".equals(version.status()))
                .findFirst()
                .orElseThrow()
                .id();
        return templateService.createDraft(new TemplateService.CreateDraftRequest(
                "NOTICE",
                sourceTemplateVersionId,
                prefix + System.nanoTime(),
                "自动化测试草稿"
        ));
    }
}
