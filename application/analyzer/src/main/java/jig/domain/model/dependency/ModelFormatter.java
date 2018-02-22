package jig.domain.model.dependency;

public interface ModelFormatter {

    String header();

    String format(Model model);

    String footer();
}
