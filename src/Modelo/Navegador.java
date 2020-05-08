package Modelo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.wait.wait;

public class Navegador {
    /*RETORNO*/
    public String observacao = "";
    
    /* CONFIGURAÇÕES DRIVER */
    private static WebDriver driver = null;
    private static WebElement e = null;
    private static List<WebElement> es = null;
    private static JavascriptExecutor js;
    
    /* PASTAS */
    private static final String pasta_downloads = "C:\\Users\\" + System.getProperty("user.name") + "\\Downloads";
    private static final String pasta_desktop = "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop";
    public static long nro_Downloads = 0;
    
    
    /*FUNÇÕES DE CONTROLE*/
    
    
    /*FUNÇÕES DO DRIVER*/
    public static void abre_navegador(){
        try{
            System.setProperty("webdriver.chrome.driver", ".\\chromedriver.exe");
            
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-print-preview");
            driver = new ChromeDriver(options);
            /*driver.manage().window().fullscreen();*/
            
            if (driver instanceof JavascriptExecutor) {
                js = (JavascriptExecutor)driver;
            }
        }catch(Exception e){
            System.out.println("Ocorreu um erro: \n " + e);
        }
    }
    public static void fecha_navegador(){
        try{
            if(driver_aberto()){
                driver.quit();
            }
        }catch(Exception e){
            System.out.println("Ocorreu um erro: \n " + e);
        }
    }
    public static boolean driver_aberto(){
        boolean b = false;
        try{
            String name_page = driver.getWindowHandle();
            b = true;
        }catch(Exception e){}
        return b;
    }
    
    
    /*NAVEGAÇÃO*/
    public static boolean faz_login_cielo(){
        boolean b = false;
        
        try{
            String nroEstab = "";
            String login = "";
            String senha = "";

            driver.get("https://minhaconta2.cielo.com.br/login/");

            e = wait.element(driver, By.name("clientesNumeroDoEstabelecimento"));
            if(e != null){
                e.sendKeys(nroEstab);

                driver.findElement(By.name("clientesUsuarioEstabelecimento")).sendKeys(login);

                driver.findElement(By.name("clientesSenha")).sendKeys(senha);

                e = wait.element(driver, By.cssSelector("form[name='vm.formLogin'] button"));
                if (e != null) {
                    e.click();
                    e = wait.element(driver, By.id("nomelogado"));
                    if(e != null){
                        b= true;
                    }
                } 
            }
        }catch(Exception e){
            System.out.println("Erro no login cielo: "  + e);
        }
        
        return b;
    }
    public static boolean faz_login_getnet(){
        boolean b = false;
        
        try{
            String login = "";
            String senha = "";

            driver.get("https://www.santandergetnet.com.br/site/index");
            
            e = wait.element(driver, By.name("edtLogin"));
            if (e != null){
                e.sendKeys(login);
                
                driver.findElement(By.name("edtSenha")).sendKeys(senha);
                
                driver.findElement(By.name("entrar")).click();
                
                e = wait.element(driver, By.id("divGetNetBarBottom"));
                if(e != null){
                    b = true;
                }
            }
        }catch(Exception e){
            System.out.println("Erro no login cielo: "  + e);
        }
        
        return b;
    }
    
    public static boolean baixar_cielo(String tipo_arquivo, int mes, int ano, String pasta_para_salvar){
        boolean b = false;
        
        int nro_tipo_arquivo = "recebimentos".equals(tipo_arquivo)?2:1;
        
        try{
            if(acessa_extratos_personalizados_cielo()){
                //seleciona tipo arquivo
                e = wait.element(driver, By.cssSelector("#mainElement > section > div > div.row.no-pad.ci-padding10-lat.ci-space20-bottom.ci-space10-top > div > table > tbody > tr:nth-child(" + nro_tipo_arquivo + ") > td:nth-child(4) > div"));
                if (e != null) {
                    e.click();
                    //seleciona data
                    if(setDateRangeCielo(mes, ano)){
                        b = clica_baixar_arquivo_cielo(tipo_arquivo, mes, ano, pasta_para_salvar);
                    } 
                }

            }
        }catch(Exception e){
            System.out.println("Erro no java ao baixar " + tipo_arquivo + " cielo " + mes + ano + ". Erro: " + e);
            e.printStackTrace();
        }
        
        return b;
    }
    public static boolean baixar_getnet(String tipo_arquivo, int mes, int ano, String pasta_para_salvar){
        boolean b = false;
        
        String link_tipo_arquivo = "recebimentos".equals(tipo_arquivo)?"liquidacao":"minhas-vendas";
        String nome_tipo_arquivo = "recebimentos".equals(tipo_arquivo)?"LCT":"CVS";
        
        try{
            driver.get("https://www.santandergetnet.com.br/novos-extratos/" + link_tipo_arquivo);
            
            String mesano = "" + (mes<10?"0" + mes:mes) + ano;
            int udia_int = ultimo_dia_mes(mes, ano);
            String udia = "" + (udia_int<10?"0" + udia_int:udia_int) ;
            
            e = wait.element(driver, By.cssSelector("#tourModal > div > div > div.modal-footer > a"),5);
            if(e != null & e.isDisplayed()){
                e.click();
            }
            
            //data inicio e fim
            
            e = wait.element(driver, By.cssSelector("#data_de"));
            if (e != null) {
                e.click();
                e.sendKeys("01" + mesano + udia + mesano);
                    
                //escolhe formato csv
                e = wait.element(driver, By.cssSelector("#formato-csv"));
                if (e != null) {
                    e.click();
                    
                    //escolhe detalhemento por filial
                    e = wait.element(driver, By.cssSelector("#apresentacao-3"));
                    if (e != null) {
                        e.click();
                        
                        //botao  pesquisar
                        e = wait.element(driver, By.cssSelector("#pesquisar"));
                        if (e != null) {
                            e.click();

                            //espera botao de download se nao achar refresh
                            e = wait.element(driver, By.cssSelector("#divContainer > table > tbody > tr:nth-child(1) > td:nth-child(5) > a"),20,true);
                            if (e != null) {

                                String anomes = "" + ano + (mes<10?"0" + mes:mes);
                                String inicio_yyyymmdd = anomes + "01";
                                String fim_yyyymmdd = anomes + udia;

                                e  = identifica_arquivo_download_getnet(nome_tipo_arquivo,inicio_yyyymmdd,fim_yyyymmdd);
                                if(e != null){
                                    b = espera_salva_download(pasta_para_salvar + "\\getnet " + tipo_arquivo + " " + mesano + ".csv",
                                                                "csv");   
                                }
                            }
                        }
                    } 
                }
            }
        }catch(Exception e){
            System.out.println("Erro no java ao baixar " + tipo_arquivo + " getnet " + mes + ano + ". Erro: " + e);
            e.printStackTrace();
        }
        
        return b;
    }
    /* UTILITARIOS NAVEGAÇÃO */
    //Cielo
    private static boolean acessa_extratos_personalizados_cielo(){
        boolean b = false;
        
        driver.get("https://minhaconta2.cielo.com.br/minha-conta/home");

        //Abre menu
        e = wait.element(driver, By.cssSelector("#navbar > ul > li:nth-child(1) > a > div"));
        if(e!=null){
            e.click(); 
            //Clica na opção escondida para ir para os extratos
            e = wait.element(driver, By.cssSelector("#navbar > ul > li.ng-scope.dropdown.open > ul > div > div > ul > li:nth-child(3)"));
            if (e != null) {
                e.click();

                //Seleciona consultar extratos personalizados
                e = wait.element(driver, By.cssSelector("#navbar > ul > li.ng-scope.dropdown.open > ul > div > div > ul > li:nth-child(3) > ul > div > div > ul > li:nth-child(2) > a"));
                if (e != null) {
                    e.click();
                    b = true;
                }
            }
        }
        return b;
    }
    private static boolean setDateRangeCielo(int mes, int ano){
        boolean b = false;
        
        e = wait.element(driver, By.cssSelector("#calendario-extrato"));
        if(e != null){
            e.click();
        
            String ultimo_dia = String.valueOf(ultimo_dia_mes(mes, ano));

            int wek_1 = dia_da_semana(1, mes, ano);
            int wek_u = dia_da_semana(ultimo_dia_mes(mes, ano), mes, ano);

            wek_1 = wek_1==7?1:wek_1+1;
            wek_u = wek_u==7?1:wek_u+1;

            String[] nome_mes = new String[]{"","Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez"};
            String nome_periodo = nome_mes[mes] + " " + ano;

            for (int i = 0; i < 10; i++) {
                //pega periodo
                e = wait.element(driver, By.cssSelector("body > div.daterangepicker.dropdown-menu.opensright.show-calendar > div.row.calendar-months > div.calendar.left > div.calendar-table > table > thead > tr:nth-child(1) > th.month"));
                if(e != null){
                    //Verifica se nome do periodo é igual
                    if(e.getAttribute("innerText").equals(nome_periodo)){
                        //String dia_procurado = "1";

                        //verifica dia 1
                        for (int j = 1; j <= 2; j++) {
                            e = pega_dia_DateRange_cielo(j,wek_1);
                            if(e.getAttribute("innerText").equals("1")){
                                e.click();
                                break;
                            }
                        }
                        for (int j = 5; j <= 6; j++) {
                            e = pega_dia_DateRange_cielo(j,wek_u);
                            if(e.getAttribute("innerText").equals(ultimo_dia)){
                                e.click();
                                break;
                            }
                        }

                        //clica no botao de buscar
                        esperar(1);
                        e = driver.findElement(By.cssSelector("body > div.daterangepicker.dropdown-menu.opensright.show-calendar > div.clear.row.no-pad > div.col-xs-2.btn-apply > button"));
                        e.click();
                        b = true;
                        break;
                    }else{
                        //Clica na seta para voltar um mês
                        e = wait.element(driver, By.cssSelector("body > div.daterangepicker.dropdown-menu.opensright.show-calendar > div.row.calendar-months > div.calendar.left > div.calendar-table > table > thead > tr:nth-child(1) > th.prev.available"));
                        if(e != null){
                            e.click();
                        }
                    }
                }
            }
        }
        return b;
    }
    private static boolean clica_baixar_arquivo_cielo(String nome_salvo_arquivo, int mes, int ano, String pasta_para_salvar){
        boolean b = false;
        try{
            e = wait.element(driver, By.cssSelector("#btnAllSales"));
            e.click();
        }catch(Exception e){
        }finally{
            //clica para exportar
            e = wait.element(driver, By.cssSelector("#btnExportar"));
            if(e != null){
                e.click();

                //escolhe csv
                e = wait.element(driver, By.cssSelector("#btnCSV"));
                if(e != null){
                    
                    b = espera_salva_download(pasta_para_salvar + "\\cielo " + nome_salvo_arquivo +
                                                " " + (mes>10?"0"+mes:mes) + ano + ".csv", "csv");
                }
            }
        }
        return b;
    }
    private static WebElement pega_dia_DateRange_cielo(int linha, int coluna){
        return wait.element(driver,By.cssSelector("body > div.daterangepicker.dropdown-menu.opensright.show-calendar > div.row.calendar-months > div.calendar.left > div.calendar-table > table > tbody > tr:nth-child(" + linha + ") > td:nth-child(" + coluna + ")"));
    }
    private static boolean espera_salva_download(String nome_para_salvar_arquivo, String extensao_arquivo){
        boolean b = false;
        
        atualiza_nro_downloads(extensao_arquivo);
        e.click();
        //espera download
        if(espera_Download(extensao_arquivo)){
            //renomeia
            try {
                /*Limpa arquivo na pasta se existir*/
                String nome_arquivo_movido = nome_para_salvar_arquivo;
                File arquivo_movido_antigo = new File(nome_arquivo_movido);
                if(arquivo_movido_antigo.exists()){
                    arquivo_movido_antigo.delete();
                }

                File csv_vendas = pega_ultimo_arquivo_da_pasta(pasta_downloads,extensao_arquivo);
                b = csv_vendas.renameTo(new File(nome_arquivo_movido));
            } catch (Exception e) {
                System.out.println("Não foi possivel mover o download: " + e);
            }
        }else{
            System.out.println("Não consegui identificar o download");
        }
        
        return b;
    }
    private static WebElement identifica_arquivo_download_getnet(String tipo,String inicio, String fim){
        WebElement ee = null;
        
        es = driver.findElements(By.cssSelector("#divContainer > table > tbody > tr"));
        
        try{
            for (int i = 0; i < es.size(); i++) {
                WebElement ele = es.get(i);
                ele = ele.findElement(By.cssSelector("td:nth-child(5) > a"));
                
                String a = ele.getAttribute("href");
                String[] nome_arquivo_split = a.split("/")[7].split("_");
                String tipo_comp = nome_arquivo_split[0];
                String inicio_comp = nome_arquivo_split[2];
                String fim_comp = nome_arquivo_split[3];
                
                if(tipo.equals(tipo_comp) & inicio.equals(inicio_comp) & fim.equals(fim_comp)){
                    ee = ele;
                    break;
                }
            }
        }catch(Exception e){
            System.out.println("Erro ao percorrer linhas da tabela de downloads getnet: " + e );
            e.printStackTrace();
        }
        
        return ee;
    }
    
    //GetNet
    
    /*ARQUIVOS*/
    private static boolean espera_Download(String extensao){
        boolean b =  false;
        for (int i = 0; i < 23; i++) {
            long nro_now = get_nro_downloads(extensao);
            if(nro_Downloads != nro_now){
                b = true;
                break;
            }
            esperar(1);
        }
        return b;
    }
    private static File pega_ultimo_arquivo_da_pasta(String location, String extensao) {
        int latestDate = -1;
        File[] files = null;
        try{
            File dir = new File(location);
            files = dir.listFiles();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if(file.getName().toUpperCase().contains("." + extensao.toUpperCase())){
                    if(latestDate != -1){
                        if (file.lastModified() > files[latestDate].lastModified()){
                            latestDate = i;
                        }
                    }else{
                        latestDate = i;
                    }
                } 
            }
        }catch(Exception e){}
        
        if(latestDate == -1){
            return null;
        }else{
            return files[latestDate];
        }
    }
    
    private static void atualiza_nro_downloads(String extensao){
        nro_Downloads = get_nro_downloads(extensao);
    }
    private static long get_nro_downloads(String extensao){
        long nro = 0;
        try{
            File dir = new File(pasta_downloads);
            File[] files = dir.listFiles();
            for (File file : files) {
                if(file.getName().toUpperCase().contains("." + extensao.toUpperCase())){nro++;}
            }
        }catch(Exception ex){}

        return nro;
    }
    
    /*TRATAMENTOS*/
    private String trata_numero(String str){
        str = str.replaceAll("\\.", "");
        str = str.replaceAll("-", "");
        str = str.replaceAll("/", "");
        str = str.replaceAll("\r", "");
        return str;
    }
    
    /*UTILITARIOS*/
    private void exibir_erro(String mensagem, Exception e){
        observacao += mensagem;
        System.out.println(mensagem + ": " +  e);
    }
    private void exibir_erro(String mensagem){
        observacao += mensagem;
        System.out.println(mensagem);
    }
    private static void esperar(long segundos){
        wait.java(segundos);
    }
    private void send_keys(String text){
        try{
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);

            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        }catch(Exception e){}
    }
    public static int randInt(int Min, int Max) {
        int randomNum = Min + (int)(Math.random() * ((Max - Min) + 1));
        return randomNum;
    }
    private static int ultimo_dia_mes(int mes, int ano){
        return LocalDate.of(ano,mes,1).with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
    }
    private static int dia_da_semana(int dia, int mes, int ano){
        return LocalDate.of(ano,mes,dia).getDayOfWeek().getValue();
    }
}
