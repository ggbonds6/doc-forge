package com.docforge.govdoc.template;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping("/template-packs")
    public List<TemplateService.TemplatePackSummary> listTemplatePacks() {
        return templateService.listTemplatePacks();
    }

    @GetMapping("/templates/{id}")
    public TemplateService.TemplateVersionDetail getTemplateVersion(@PathVariable String id) {
        return templateService.getTemplateVersion(id);
    }

    @PostMapping("/templates/drafts")
    public TemplateService.TemplateVersionDetail createDraft(@Valid @RequestBody CreateDraftBody body) {
        return templateService.createDraft(new TemplateService.CreateDraftRequest(body.packCode(), body.sourceTemplateVersionId(), body.name(), body.description()));
    }

    @PutMapping("/templates/{id}")
    public TemplateService.TemplateVersionDetail updateDraft(@PathVariable String id, @Valid @RequestBody UpdateTemplateBody body) {
        return templateService.updateDraft(id, new TemplateService.UpdateTemplateRequest(body.name(), body.description(), body.ruleProfile()));
    }

    @PostMapping("/templates/{id}/publish")
    public TemplateService.TemplateVersionDetail publish(@PathVariable String id, @RequestBody(required = false) PublishTemplateBody body) {
        TemplateService.PublishTemplateRequest request = body == null
                ? new TemplateService.PublishTemplateRequest(null, null)
                : new TemplateService.PublishTemplateRequest(body.overrideReason(), body.overrideReviewer());
        return templateService.publish(id, request);
    }

    @PutMapping("/templates/{id}/acceptance")
    public TemplateService.TemplateVersionDetail reviewAcceptance(@PathVariable String id, @Valid @RequestBody AcceptanceReviewBody body) {
        return templateService.reviewAcceptance(id, new TemplateService.AcceptanceReviewRequest(body.code(), body.status(), body.reviewer(), body.comment()));
    }

    public record CreateDraftBody(
            @NotBlank(message = "模板包编码不能为空") String packCode,
            String sourceTemplateVersionId,
            @NotBlank(message = "模板名称不能为空") String name,
            String description
    ) {
    }

    public record UpdateTemplateBody(
            @NotBlank(message = "模板名称不能为空") String name,
            String description,
            TemplateRuleProfile ruleProfile
    ) {
    }

    public record AcceptanceReviewBody(
            @NotBlank(message = "验收项编码不能为空") String code,
            @NotBlank(message = "验收状态不能为空") String status,
            String reviewer,
            String comment
    ) {
    }

    public record PublishTemplateBody(
            String overrideReason,
            String overrideReviewer
    ) {
    }
}
