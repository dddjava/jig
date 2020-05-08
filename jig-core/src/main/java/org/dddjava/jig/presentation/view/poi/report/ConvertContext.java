package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.application.service.AliasService;

public class ConvertContext {
    public AliasService aliasService;

    public ConvertContext(AliasService aliasService) {
        this.aliasService = aliasService;
    }
}
