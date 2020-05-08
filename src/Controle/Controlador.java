package Controle;

import Modelo.Navegador;
import Visao.carregando;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import main.Arquivo;

public class Controlador {
    public static String mensagem_final  = "";
    
    public static String pasta = "";
    private static File pastaFile = null;
    private static String link_pasta_selecionada = "";
    
    public static boolean baixar_arquivos = false;
    
    private static String[] meses_com_vendas = null;
    private static int mes;
    private static int ano;
    
    private static final String[] bancos_ofx  = "banri;bb;std".split(";");
    
    private static final String nome_arquivo_sistema = "giro";
    private static File arquivo_sistema = null;
    private static String[] linhas_arquivo_sistema = null;
    
    
    public static void iniciar(){
        if (Selector.Pasta.verifica(pasta)) {
            pastaFile = new File(pasta);
            link_pasta_selecionada = "'" + pasta + "'";
            try{
                //exibe carregamento
                JFrame carregamento = new carregando();
                carregamento.setVisible(true);
                
                Executar_Funcoes();

                //esconde carregamento
                carregamento.setVisible(false);
        
                if(!"".equals(mensagem_final)){
                    JOptionPane.showMessageDialog( null ,
                        mensagem_final,
                        " Programa Finalizado!" , JOptionPane.INFORMATION_MESSAGE );
                }
                //System.exit(0);
            }catch(Exception e){
                JOptionPane.showMessageDialog( null ,
                    " OCORREU UM ERRO NÃO ESPERADO NO JAVA! ERRO: \n" + e,
                    " ERRO NO JAVA!" , JOptionPane.WARNING_MESSAGE );
                e.printStackTrace();
            }
        }
    }
    
    /*FUNÇÕES*/
    private static void Executar_Funcoes(){
        //if (SetConfig()){
            //VERIFICA ARQUIVOS
            if (Verificar_Arquivos()){
                //CONFIGURA ARRAY GIRO
                if(Configurar_array_sistema()){
                    //BAIXA VENDAS E RECEBIMENTOS
                    if(BaixarVendasRecebimentos(baixar_arquivos)){
                        
                        //IDENTIFICA A MÁQUINA DE CARTÃO
                        /*if(IdentificarMaquinaCartao()){*/

                            //COMPARA VALROES COM RECEBIMENTOS DAS MAQUINAS
                            if(ComparaValoresComRecebimentosMaquinas()){
                                //COMPARA VALORES COM OFX DOS BANCOS

                                    //RETORNA CSV COM INFORMAÇÕES DAS NFS BAIXADAS
                                    if(Salvar_Ultimo_Estado_Linhas_Giro()){
                                        mensagem_final = "Arquivo CSV gerado na pasta escolhida!";
                                    }else{
                                        Erro("Erro ao salvar arquivo giro em seu ultimo estado.");
                                    }
                            }
                        /*}else{
                            Erro("Erro ao identificar as máqunas de cartão de credito!");
                        }*/
                    }else{
                        Erro("Erro ao baixar vendas e recebimentos cielo e getnet.");
                    }
                }else{
                    mensagem_final = "Problemas ao abrir/editar o arquivo do Giro.";
                }
            }else{
                mensagem_final = "Erro nos arquivos!\n" + mensagem_final ;
            }
        //}
    }
    
    private static boolean Verificar_Arquivos(){
        boolean b = true;
        
        //Arquivos OFX
        System.out.println("Encontrando arquivos OFX");
        for (String nome_banco : bancos_ofx) {
            File arquivoBanco = Selector.Pasta.procura_arquivo(pastaFile, nome_banco + ";.ofx");
            if(arquivoBanco == null){
                b = false;
                mensagem_final = "Não encontrei o arquivo do banco '" + nome_banco + "' na "
                        + link_pasta_selecionada;
                break;
            }    
        }
        
        System.out.println("Encontrando arquivo Giro");
        //Arquivo Sistema
        if(b == true){
            arquivo_sistema = Selector.Pasta.procura_arquivo(pastaFile, nome_arquivo_sistema + ";.csv");
            if(arquivo_sistema ==  null){
                b = false;
                mensagem_final = "Não encontrei o arquivo do sistema ("  +nome_arquivo_sistema + ") na "
                        + link_pasta_selecionada;
            }
        }
        
        System.out.println("Encontrando arquivo Todas Poa.csv");
        //Arquivos VERO
        /* Não irá comparar vendas VERO por enquanto */
        if(b == true){
            File vero_recebimentos = Selector.Pasta.procura_arquivo(pastaFile, "todas;poa" + ";.csv");
            if(vero_recebimentos ==  null){
                b = false;
                mensagem_final = "Não encontrei o arquivo de recebiemntos VERO(TODOS POA MESANO.CSV) na "
                        + link_pasta_selecionada;
            }
        }
        
        
        return b;
    }
    private static boolean Configurar_array_sistema(){
        boolean b = true;
        
        System.out.println("Configurando array Giro");
        //Pega Texto Arquivo
        try{
            String texto_arquivo_sistema = Arquivo.ler(arquivo_sistema.getAbsolutePath());
            
            texto_arquivo_sistema = texto_arquivo_sistema.replaceAll(",\"", ";");
            texto_arquivo_sistema = texto_arquivo_sistema.replaceAll("\"", "");
            
            String[] linhas_texto_arquivo = texto_arquivo_sistema.split("\n");
            
            String texto_tratado = "";
            String vencimento = "";
            
            for (String linha : linhas_texto_arquivo) {
                String[] colunas = linha.split(";");
                if(colunas[0].contains("Vencimento:")){
                    vencimento = colunas[0].substring(colunas[0].length() - 10, colunas[0].length());
                }
                if(!"".equals(vencimento) & colunas.length >= 14){
                    if(!"".equals(colunas[0]) & isNumeric(colunas[1].replaceAll("/", "")) &
                        /*isNumeric(colunas[4].replaceAll("/", "")) &*/ !"".equals(colunas[5]) & !"".equals(colunas[14])){
                        texto_tratado += "".equals(texto_tratado) ? "" : "\n"; //pulo linha
                        texto_tratado += colunas[0] + ";" ; //nf
                        texto_tratado += colunas[1] + ";" ; //Data Venda
                        texto_tratado += colunas[4] + ";" ; //Data Recebimento
                        texto_tratado += vencimento + ";" ; //Data Vencimento
                        texto_tratado += colunas[5] + ";" ; //Historico
                        texto_tratado += colunas[14] ; //Valor
                    }
                }
                
            }
            
            linhas_arquivo_sistema = texto_tratado.split("\n");
        }catch(Exception e){
            System.out.println("Problemas ao configurar o array do sistema: " + e);
            b = false;
        }
        
        return b;
    }
    
    private static boolean BaixarVendasRecebimentos(boolean baixar){
        boolean b = false;
        
            String meses_str = "";
            
            //define mes recebimentos
            String primeira_data = linhas_arquivo_sistema[0].split(";")[3];

            mes = Integer.valueOf(primeira_data.substring(3, 5));
            ano = Integer.valueOf(primeira_data.substring(6, 10));
            
            //Percorre arquivo Giro
            for (String linha : linhas_arquivo_sistema) {
                String[] colunas = linha.split(";");
                //Anota os meses que aparecem das vendas
                if(meses_str.contains("#" + colunas[1].substring(3, 10) + "#") == false){
                    meses_str += "#" + colunas[1].substring(3, 10) + "#";
                }
            }

            meses_str = meses_str.replaceAll("##", ";");
            meses_str = meses_str.replaceAll("#", "");

            //Mostra Periodos
            System.out.println("Peridodos para baixar: " + meses_str);

            meses_com_vendas = meses_str.split(";");

            if(meses_com_vendas.length > 1){
                if(baixar){
                    //Abre navegador
                    Navegador.abre_navegador();

                    //Faz logins
                    if ( Navegador.faz_login_cielo() & Navegador.faz_login_getnet()){
                        //baixa vendas
//                        for (String mes_ano : meses_com_vendas) {
//                            int mes = Integer.valueOf(mes_ano.substring(0, 2));
//                            int ano = Integer.valueOf(mes_ano.substring(3, 7));
//
//                            if( Navegador.baixar_cielo("vendas",mes, ano, pasta) & //cielo
//                                Navegador.baixar_getnet("vendas",mes, ano, pasta) //getnet
//                                )
//                            {
                                b = true;
//                            }else{
//                                b = false;
//                                break;
//                            }
//                        }

                        //baixa recebimentos do mes
                        if(b == true){
                            b = false;

                            if(Navegador.baixar_cielo("recebimentos",mes, ano, pasta) &
                               Navegador.baixar_getnet("recebimentos",mes, ano, pasta)
                               ){
                                b = true;
                            }
                        }
                    }

                    //Fecha Navegador
                    Navegador.fecha_navegador();
                }else{
                    b = true;
                }
            }
        
        return b;
    }
    
    private static boolean IdentificarMaquinaCartao(){
        boolean b = false;
        
        try{
            //Arruma arquivos_str GETNET e Cielo para verificação
            String arquivos_getnet = Get_Arquivos_Maquina("GETNET","vendas",1,3,4,15,19,1,19);
            String arquivos_cielo = Get_Arquivos_Maquina("CIELO","vendas",6,2,3,4,5,7,7);

            String arquivos_str = "";
            if(!"".equals(arquivos_getnet)){
                arquivos_str = arquivos_getnet;
            }
            if(!"".equals(arquivos_cielo)){
                arquivos_str += ("".equals(arquivos_str)?"":"§") + arquivos_cielo;
            }

            if(!"".equals(arquivos_str)){
                String[] arquivos = arquivos_str.split("§");
                String valores_contabilizados = ""; 
                //Percorre arquivo Giro
                for (int i = 0; i < linhas_arquivo_sistema.length;i++){
                    boolean breaker = false;
                    String linha = linhas_arquivo_sistema[i];
                    String[] colunas = linha.split(";");
                    String data_giro = colunas[1];
                    String competencia = data_giro.substring(3, 10);
                    Double valor_giro = getDouble(colunas[5]);

                    for (String arquivo : arquivos) {
                        String[] dados_arquivo = arquivo.split("###");
                        if(dados_arquivo[1].equals(competencia)){
                            String[] linhas_arquivo = dados_arquivo[2].split("\n");
                            for (int j = 0; j < linhas_arquivo.length; j++) {
                                String lin = linhas_arquivo[j];
                                String[] col_lin = lin.split(";");
                                String dt_arq = col_lin[0];
                                Double val_arq = getDouble(col_lin[2]);
                                String contab = "#" + dados_arquivo[0]+ competencia + j + "#";
                                
                                //Se valor e dia forem iguais e nao tiver maquininha na ultima coluna
                                if(data_giro.equals(dt_arq) & Objects.equals(val_arq, valor_giro) 
                                        & !valores_contabilizados.contains(contab)){
                                    //coloca a maquininha na ultima coluna
                                    linhas_arquivo_sistema[i] += ";" + dados_arquivo[0];
                                    valores_contabilizados +=  contab;
                                    //aplica taxa no valor
                                    breaker = true;
                                    break;
                                }
                            }
                        }
                        if(breaker){break;}
                    }
                    if(!breaker){
                        linhas_arquivo_sistema[i] += ";" + "BANRISUL";
                    }
                }
                b = true;
            }
        }catch(Exception e){
            System.out.println("Erro ao identificar as máquinas de vendas: " + e );
            e.printStackTrace();
        }
        return b;
    }
    private static boolean ComparaValoresComRecebimentosMaquinas(){
        boolean b = false;
        
        try{
            //altera os "meses_com_vendas"
            meses_com_vendas = new String[]{(mes<10?"0"+mes:mes) + "/" + ano};
            //Pega informações necessárias dos recebimentos
            String recebimentos_getnet = Get_Arquivos_Maquina("GETNET","recebimentos",1,2,2,13,19,1,19);
            String recebimentos_cielo = Get_Arquivos_Maquina("CIELO","recebimentos",4,2,1,8,9,3,9);
            String recebimentos_banrisul = Get_Arquivos_Maquina("TODAS","POA",2,0,0,3,6,1,6);

            String recebimentos_str = "";
            if(!"".equals(recebimentos_getnet)){
                recebimentos_str = recebimentos_getnet;
            }
            if(!"".equals(recebimentos_cielo)){
                recebimentos_str += ("".equals(recebimentos_str)?"":"§") + recebimentos_cielo;
            }
            
            if(!"".equals(recebimentos_banrisul)){
                recebimentos_str += ("".equals(recebimentos_str)?"":"§") + recebimentos_banrisul;
            }
            

            if(!"".equals(recebimentos_str)){
                String[] arquivos = recebimentos_str.split("§");
                String valores_contabilizados = ""; 
                //Percorre arquivo Giro
                for (int i = 0; i < linhas_arquivo_sistema.length;i++){
                    //configura linha
                    String linha = linhas_arquivo_sistema[i];
                    String[] colunas = linha.split(";");
                    
                    //Define valores
                    String data_giro =  colunas[3].substring(6,10) + "-" +
                                        colunas[3].substring(3, 5) + "-" +
                                        colunas[3].substring(0, 2);
                    String competencia = colunas[3].substring(3, 10);
                    Double valor_giro = getDouble(colunas[5]);
                    
                    
                    //Encontra valor giro com data original
                    String data_comparada = data_giro;
                    //Se nao encontrar
                    if(EncontraValorGiroNasMaquinas(arquivos, competencia, valores_contabilizados,
                                                    data_comparada, valor_giro, i) == false){
                        //Define data giro com a data -1 dia
                        data_comparada = LocalDate.parse(data_giro).minusDays(1).toString();
                        //Se não encontrar valor giro com data -1
                        if(EncontraValorGiroNasMaquinas(arquivos, competencia, valores_contabilizados,
                                                        data_comparada, valor_giro, i) ==  false){
                            //define data com data + 1
                            data_comparada = LocalDate.parse(data_giro).plusDays(1).toString();
                            //Se não encontrar data +1
                            if(EncontraValorGiroNasMaquinas(arquivos, competencia, valores_contabilizados,
                                                        data_comparada, valor_giro, i) ==  false){
                                //Define data +2
                                data_comparada = LocalDate.parse(data_giro).plusDays(2).toString();
                                //se nao encontrar data +2
                                if(EncontraValorGiroNasMaquinas(arquivos, competencia, valores_contabilizados,
                                                        data_comparada, valor_giro, i) ==  false){
                                    //define data +3
                                    data_comparada = LocalDate.parse(data_giro).plusDays(3).toString();
                                    //tenta encontrar data +3
                                    EncontraValorGiroNasMaquinas(arquivos, competencia, valores_contabilizados,
                                                                data_comparada, valor_giro, i);
                                }
                            }
                        }
                    }
                }
                b = true;
            }
                
        }catch(Exception e){
            System.out.println("Erro ao comparar valores: " + e);
        }
        
        return b;
    }
    
    private static boolean EncontraValorGiroNasMaquinas(String[] arquivos, String competencia,
                                                        String valores_contabilizados, String data_giro,
                                                        Double valor_giro, int linha_arquivo_giro){
        boolean b = false;
        
        data_giro =  data_giro.substring(0,2) + "/" +
                            data_giro.substring(3, 5) + "/" +
                            data_giro.substring(6, 10);
        
        try{
            //Percorre arquivos de recebimentos
            for (String arquivo : arquivos) {
                boolean breaker  = false;
                String[] dados_arquivo = arquivo.split("###");
                //Verifica competencia do arquivo
                if(dados_arquivo[1].equals(competencia)){
                    //Pega linhas do arquivo
                    String[] linhas_arquivo = dados_arquivo[2].split("\n");
                    for (int j = 0; j < linhas_arquivo.length; j++) {
                        String lin = linhas_arquivo[j];
                        String[] col_lin = lin.split(";");

                        //Define valores do arquivo
                        String dt_pag = col_lin[1];
                        Double valor_bruto = getDouble(col_lin[2]);
                        Double valor_liq = getDouble(col_lin[3]);

                        String tipo_pagamento = col_lin[4];
                        String bandeira = col_lin[5];


                        String contab = "#" + dados_arquivo[0]+ competencia + j + "#";

                        //Se o valor não tiver sido utilizado ainda
                        if(!valores_contabilizados.contains(contab)){
                            //Se achar o valor
                            if(data_giro.equals(dt_pag) & 
                                    valor_giro.toString().equals(valor_bruto.toString()) ){
                                /*taxas*/
                                Double valor_taxa = getTaxa(valor_bruto, dados_arquivo[0], tipo_pagamento, bandeira);
                                Double valor_com_taxa = round(valor_giro - valor_taxa,2);


                                //comparações com taxas
                                linhas_arquivo_sistema[linha_arquivo_giro] += ";" + dados_arquivo[0]; //nome maquina
                                linhas_arquivo_sistema[linha_arquivo_giro] += ";" + bandeira; //BANDEIRA
                                linhas_arquivo_sistema[linha_arquivo_giro] += ";" + tipo_pagamento; //tipo_pagamento
                                linhas_arquivo_sistema[linha_arquivo_giro] += ";" + valor_liq.toString().replaceAll("\\.", ",") ; //valor_liquido da venda
                                linhas_arquivo_sistema[linha_arquivo_giro] += ";" + valor_com_taxa.toString().replaceAll("\\.", ","); //valor do bruto com taxa
                                linhas_arquivo_sistema[linha_arquivo_giro] += ";" + round(valor_taxa,2); //Porcentagem da taxa

                                valores_contabilizados +=  contab;
                                b = true;
                                breaker =  true;
                                break;
                            }
                        }
                    }
                }
                if(breaker){break;}
            }
        }catch(Exception e){
            System.out.println("Erro ao encontrar valor giro na linha" + linha_arquivo_giro + " com valor "  + valor_giro + 
                    "Erro:" + e);
            e.printStackTrace();
        }
        return b;
    }
    
    /*AUXILIARES DAS FUNÇÕES*/
    private static String Get_Arquivos_Maquina(String maquina, String tipo_arquivo,
                            int col_tipo_pag, int col_data_venda, int col_data_pag,
                            int col_val_bruto, int col_val_liq,int col_bandeira,
                            int min_len){
        String recebimentos = "";
        
        try {
                        //Listar getnet da pasta
            String textos_arquivos = "";

            //percorre periodos
            for (String periodo : meses_com_vendas) {
                int mes = Integer.valueOf(periodo.substring(0, 2));
                int ano = Integer.valueOf(periodo.substring(3, 7));
                
                String vendas_periodo = "";
                
                //encontra arquivo do periodo
                File arquivo_periodo = Selector.Pasta.procura_arquivo(pastaFile,
                                                        maquina + ";" + tipo_arquivo + ";" + mes + ano);
                if(arquivo_periodo != null){
                    String texto_arquivo_periodo = Arquivo.ler(arquivo_periodo.getAbsolutePath());
                    String[] linhas_texto = texto_arquivo_periodo.split("\n");
                    for (String linha : linhas_texto) {
                        String[] colunas = linha.split(";");
                        
                        //verifica se tem todas colunas de comparação
                        if(colunas.length >= min_len){
                            if(colunas[col_data_pag].length() >= 10 &
                                colunas[col_data_venda].length() >= 10){
                                String tipo_pag = colunas[col_tipo_pag].replaceAll("\"", "");
                                String bandeira = colunas[col_bandeira].replaceAll("\"", "");
                                String data_pag = colunas[col_data_pag].replaceAll("\"", "").substring(0,10);
                                String data_venda = colunas[col_data_venda].replaceAll("\"", "").substring(0,10);
                                Double val_bruto = getDouble(colunas[col_val_bruto].replaceAll("\"", ""));
                                Double val_liq = getDouble(colunas[col_val_liq].replaceAll("\"", ""));

                                //faz comparações
                                if(
                                    (!"".equals(tipo_pag) & !tipo_pag.toUpperCase().contains("DÉBITO")) & /*TIPO PAG*/
                                    isDate(data_pag) & /*DATA PAGAMENTO*/
                                    (val_bruto != 0.0) & /*VALOR BRUTO*/
                                    (val_liq != 0.0) /*VALOR LIQUIDO*/
                                    ){
                                    //adiciona na variavel de vendas do periodo
                                    vendas_periodo += ("".equals(vendas_periodo)?"":"\n");
                                    vendas_periodo += data_venda + ";";
                                    vendas_periodo += data_pag + ";";
                                    vendas_periodo += val_bruto + ";";
                                    vendas_periodo += val_liq + ";";
                                    vendas_periodo += tipo_pag + ";";
                                    vendas_periodo += bandeira;
                                }
                            }
                        }
                    }
                }
                
                //se as vendas do periodo nao forem nulas, adiciona no texto arquivos_str
                if(!"".equals(vendas_periodo)){
                    textos_arquivos += ("".equals(textos_arquivos)?"":"§");
                    textos_arquivos += maquina + "###";
                    textos_arquivos += periodo + "###";
                    textos_arquivos += vendas_periodo;
                }
            }
            
            if(!"".equals(textos_arquivos)){
                recebimentos = textos_arquivos;
            }
            
        } catch (Exception e) {
            System.out.println("Erro ao buscar " + tipo_arquivo + " da maquina " + maquina + ":" + e);
            e.printStackTrace();
        }
        
        return recebimentos;
    }
    
    private static Double getTaxa(Double valor,String nome_maquina, String tipo_pagamento,String bandeira){
        /*tratamento*/
        String band = bandeira.toUpperCase();
        String maquina = nome_maquina.toUpperCase();
        String tipo = tipo_pagamento.toUpperCase();
        
        Double taxa = 0.0;
        
        int tp = 0;
        if(tipo.contains("DÉBITO") | tipo.contains("DEBITO")){tp = 2;}
        else if(tipo.contains("VISTA")){tp = 3;}
        else if(tipo.contains("PARC")){tp = 4;}
        
        if(tp != 0){
            String str_config =
            "GETNET;VISA;0,0090;0,0150;0,0185\n" +
            "GETNET;MASTER;0,0090;0,0150;0,0185\n" +
            "GETNET;ELO;0,0090;0,0150;0,0185\n" +
            "GETNET;EXPRESS;0,0000;0,0150;0,0185\n" +
            "GETNET;HIPER;0,0000;0,0150;0,0185\n" +
            "CIELO;VISA;0,0105;0,0160;0,0210\n" +
            "CIELO;MASTER;0,0105;0,0160;0,0210\n" +
            "CIELO;ELO;0,0105;0,0160;0,0210\n" +
            "CIELO;EXPRESS;0,0000;0,0282;0,0465\n" +
            "CIELO;HIPER;0,0000;0,0365;0,0465\n" +
            "TODAS;VISA;0,0135;0,0165;0,0280\n" +
            "TODAS;MASTER;0,0135;0,0165;0,0280\n" +
            "TODAS;BANRI;0,0145;0,0165;0,0280\n" +
            "TODAS;ELO;0,0260;0,0320;0,0400\n" +
            "TODAS;BANRICARD;0,0000;0,0500;0,0000\n" +
            "TODAS;VERDECAR;0,0400;0,0400;0,0400";

            String[] linhas_config_taxas = str_config.split("\n");
            for (String lin : linhas_config_taxas) {
                String[] cols = lin.split(";");
                if(maquina.contains(cols[0]) & band.contains(cols[1])){
                    taxa = getDouble(cols[tp]);
                    break;
                }
            }

        }
        Double valor_taxa = valor * taxa;
        return valor_taxa;
    }
    private static boolean Salvar_Ultimo_Estado_Linhas_Giro(){
        boolean b = false;
        
        try {
            String linhas = "NF;DATA VENDA;DATA VENCIMENTO;DATA RECEBIMENTO;HISTORICO;VALOR;MAQUINA;BANDEIRA;PAGAMENTO;LIQUIDO MAQUINA;LIQUIDO CALCULADO;TAXA CALCULADA";
            for (String linha : linhas_arquivo_sistema) {
                linhas += "\n" + linha;
            }
            
            b = Arquivo.salvar(pasta + "\\GIRO ULTIMO ESTADO.csv",linhas);
            
        } catch (Exception e) {
            System.out.println("Erro ao tentar salvar ultimo estado linhas giro: "  + e);
            e.printStackTrace();
        }
        
        return b;
    }
    
    /* UTILITÁRIOS */
    private static void Erro(String erro){
        JOptionPane.showMessageDialog( null ,
                    erro,
                    " ERRO!" , JOptionPane.ERROR_MESSAGE);
    }
    public static String ultimo_dia_mes(int mes, int ano){
        return LocalDate.of(ano,mes,1).with(TemporalAdjusters.lastDayOfMonth()).toString();
    }
    public static String primeiro_dia_mes(int mes, int ano){
        return LocalDate.of(ano,mes,1).toString();
    }
    public static boolean isNumeric (String s) {
        try {
            Long.parseLong (s); 
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
    public static boolean isDate(String date){
        return isDate(date,"dd/MM/yyyy");
    }
    public static boolean isDate(String date, String format_date){
        DateFormat format = new SimpleDateFormat(format_date);
        format.setLenient(false);
        try {
            format.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static Double getDouble(String s){
        Double b;
        String return_Double = "";
        try{
            String[] numbers = new String[]{"0","1","2","3","4","5","6","7","8","9",".",","};
            s = s.replaceAll(" ", "");
            
            String[] split_s = s.split("");
            for (String charr : split_s) {
                for (String num : numbers) {
                    if(charr.equals(num)){
                        return_Double += charr;
                        break;
                    }
                }
            }
            
            int pontos = ocorrencias(return_Double, "\\.");
            int virgulas = ocorrencias(return_Double, ",");
            
            if(pontos > 1 | virgulas > 1){
                //verifica ocorrencias de casda um para tirar ponto milhar
                return_Double = "";
            }else if(pontos == 1 & virgulas == 1){
                return_Double = return_Double.replaceAll("\\.", "");
            }

        }catch(Exception e){
            return_Double = "";
        }
        
        if(!"".equals(return_Double)){
            try{
                b = Double.valueOf(return_Double.replaceAll(",","."));
            }catch(Exception e){
                b = 0.0;
            }
        }else{b = 0.0;}
         
        return b;
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    public static int ocorrencias(String s,String expressao){
        return (s.length() - s.replaceAll(expressao, "").length()) / expressao.length();
    }
}
