package pt.ipleiria.dei.ei.estg.researchcenter.entities;

public enum PublicationType {
    ARTICLE("Artigo Científico", "Artigo científico com revisão por pares"),
    CONFERENCE("Comunicação em Conferência", "Apresentação ou paper de conferência"),
    BOOK_CHAPTER("Capítulo de Livro", "Capítulo em livro científico"),
    BOOK("Livro Científico", "Livro completo de caráter científico"),
    TECHNICAL_REPORT("Relatório Técnico", "Relatório técnico ou white paper"),
    PATENT("Patente", "Documento de patente"),
    DATASET("Dataset", "Conjunto de dados científicos"),
    SOFTWARE("Software", "Software em código aberto"),
    AI_MODEL("Modelo de IA", "Modelo de inteligência artificial"),
    DATABASE("Base de Dados", "Base de dados científica"),
    THESIS_MASTER("Tese de Mestrado", "Dissertação de mestrado"),
    THESIS_PHD("Tese de Doutoramento", "Tese de doutoramento"),
    OUTREACH("Artigo de Divulgação", "Artigo de divulgação científica");
    
    private final String name;
    private final String description;
    
    PublicationType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}