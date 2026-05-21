package com.docforge.govdoc.document;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentMetadata {
    private DocumentType docType;
    private String secrecyLevel;
    private String urgencyLevel;
    private String issuingAuthority;
    private String documentNumber;
    private String signer;
    private String mainRecipients;
    private String title;
    private String attachmentNote;
    private String signatory;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issuedAt;
    private List<String> copyRecipients = new ArrayList<>();
    private String printAuthority;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate printIssuedAt;
    private String note;

    public DocumentType getDocType() {
        return docType;
    }

    public void setDocType(DocumentType docType) {
        this.docType = docType;
    }

    public String getSecrecyLevel() {
        return secrecyLevel;
    }

    public void setSecrecyLevel(String secrecyLevel) {
        this.secrecyLevel = secrecyLevel;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getIssuingAuthority() {
        return issuingAuthority;
    }

    public void setIssuingAuthority(String issuingAuthority) {
        this.issuingAuthority = issuingAuthority;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getMainRecipients() {
        return mainRecipients;
    }

    public void setMainRecipients(String mainRecipients) {
        this.mainRecipients = mainRecipients;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAttachmentNote() {
        return attachmentNote;
    }

    public void setAttachmentNote(String attachmentNote) {
        this.attachmentNote = attachmentNote;
    }

    public String getSignatory() {
        return signatory;
    }

    public void setSignatory(String signatory) {
        this.signatory = signatory;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDate issuedAt) {
        this.issuedAt = issuedAt;
    }

    public List<String> getCopyRecipients() {
        return copyRecipients;
    }

    public void setCopyRecipients(List<String> copyRecipients) {
        this.copyRecipients = copyRecipients;
    }

    public String getPrintAuthority() {
        return printAuthority;
    }

    public void setPrintAuthority(String printAuthority) {
        this.printAuthority = printAuthority;
    }

    public LocalDate getPrintIssuedAt() {
        return printIssuedAt;
    }

    public void setPrintIssuedAt(LocalDate printIssuedAt) {
        this.printIssuedAt = printIssuedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

