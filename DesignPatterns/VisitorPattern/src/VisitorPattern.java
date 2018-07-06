public class VisitorPattern {
    /* Visitor Pattern
     * Q1 What is visitor pattern
     * >>> to separate an algorithm from an object on which it operates
     *
     * Q2 Benefits
     * >>> add new operations without modifying object structure
     * >>> when new operations are needed frequently and that object consists of many unrelated
     * >>> classes, to distribute these operations among all these classes is not good
     *
     * Q3 Essential
     * >>> using function overloading to conduct different behaviours
     *
     * Q4 Functions
     * >>> accept() defines traversal pattern, i.e. how to traverse all elements
     * >>> visit() defines how to visit a particular node
     *
     * Q5 Attention
     * >>> accept & visit should better use "final" parameters, as they only traverse the structure
     * >>> rather than changing them
     * */

    public static void main(String[] args) {
        Movie m1 = new ScienceFictionMovie("John Carney");
        m1.accept(new PrintMovieVisitor());
        m1.accept(new DefaultMovieVisitor());

        Movie m2 = new ComedyMovie("Amy Adams");
        m2.accept(new PrintMovieVisitor());
        m2.accept(new DefaultMovieVisitor());
    }
}

// base class & its descendants
interface Movie {
    void accept(Visitor visitor);
}

class ScienceFictionMovie implements Movie {
    String director;
    Movie[] movies;

    public ScienceFictionMovie(String director) {
        this.director = director;
    }

    @Override
    public void accept(final Visitor visitor) {
        if (movies != null) for (Movie movie: movies) movie.accept(visitor);
        visitor.visit(this);
    }
}

class ComedyMovie implements Movie {
    String star;
    Movie[] movies;

    public ComedyMovie(String star) {
        this.star = star;
    }

    @Override
    public void accept(final Visitor visitor) {
        if (movies != null) for (Movie movie: movies) movie.accept(visitor);
        visitor.visit(this);
    }
}

// base visitor
interface Visitor {
    void visit(ScienceFictionMovie scienceFictionMovie);
    void visit(ComedyMovie comedyMovie);
}

// different behaviours implemented via different subclass of visitor
class PrintMovieVisitor implements Visitor {
    public void visit(final ScienceFictionMovie scienceFictionMovie) {
        if (scienceFictionMovie == null) return;
        System.out.println("Director: " + scienceFictionMovie.director);
    }

    public void visit(final ComedyMovie comedyMovie) {
        if (comedyMovie == null) return;
        System.out.println("Star: " + comedyMovie.star);
    }
}

class DefaultMovieVisitor implements Visitor {
    @Override
    public void visit(ScienceFictionMovie scienceFictionMovie) {
        System.out.println("Default director is " + scienceFictionMovie.director);
    }

    @Override
    public void visit(ComedyMovie comedyMovie) {
        System.out.println("Default star is " + comedyMovie.star);
    }
}
