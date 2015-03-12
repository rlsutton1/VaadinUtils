package au.com.vaadinutils.ui;


public interface AutoCompeleteQueryListener<E>
{

    void handleQuery(AutoCompleteTextField<E> field,String queryText);

}
