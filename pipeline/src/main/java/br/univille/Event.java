package br.univille;

public record Event(int x, int y, int color) {
    //método para converter um json string em um evento
    public static Event fromJson(String json) {
        // Remove "Event[" do início e "]" do final
        String content = json.substring(json.indexOf('[')+1, json.indexOf(']'));
        
        // Mapeia para armazenar os valores
        int x = 0, y = 0, color = 0;
        
        // Divide por vírgula para obter cada campo
        String[] fields = content.split(", ");
        for (String field : fields) {
            String[] keyValue = field.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                switch (key) {
                    case "x" -> x = Integer.parseInt(value);
                    case "y" -> y = Integer.parseInt(value);
                    case "color" -> color = Integer.parseInt(value);
                }
            }
        }
        
        return new Event(x, y, color);
    }
    
}