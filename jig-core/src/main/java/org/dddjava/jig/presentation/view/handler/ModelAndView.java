package org.dddjava.jig.presentation.view.handler;

public record ModelAndView(Object model, Class<? extends JigView> viewClass) {
}
