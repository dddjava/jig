package jig.analizer.dependency;

public interface ModelFormatter {

    String header();

    String format(Model model);

    String footer();
}
