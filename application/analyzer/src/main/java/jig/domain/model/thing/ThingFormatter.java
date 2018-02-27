package jig.domain.model.thing;

public interface ThingFormatter {

    String header();

    String format(Thing thing);

    String footer();
}
