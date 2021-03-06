package com.example.moneymoves.Pages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneymoves.Adapters.AdvavcedMoneyAdapter;
import com.example.moneymoves.Database.Entities.BudgetTemplate;
import com.example.moneymoves.R;
import com.example.moneymoves.ViewModels.BudgetTemplateViewModel;
import com.example.moneymoves.ViewModels.ProgressBarViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ExpensesPage extends AppCompatActivity
{

	public static final int ADD_BUDGET_REQUEST = 1;
	public static final int EDIT_BUDGET_REQUEST = 2;
	private AdvavcedMoneyAdapter adapter;
	private BudgetTemplateViewModel budgetTemplateViewModel;
	private ProgressBarViewModel progressBarViewModel;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_expenses_page);
		Toolbar toolbar = findViewById(R.id.toolbar);

		setSupportActionBar(toolbar);

		final FloatingActionButton addExpenseButton = findViewById(R.id.addExpenseButton);
		addExpenseButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View view)
			{
				Intent intent = new Intent(ExpensesPage.this, AddExpensePage.class);
				startActivityForResult(intent, ADD_BUDGET_REQUEST);
			}
		});

		final TextView x = findViewById(R.id.noteAmount);
		TextView y = findViewById(R.id.budgetCategory);
		y.setText("Income");

		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(RecyclerView.VERTICAL);

		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setHasFixedSize(true);
		adapter = new AdvavcedMoneyAdapter();
		recyclerView.setAdapter(adapter);

		// Destroys when finished
		budgetTemplateViewModel = ViewModelProviders.of(this).get(BudgetTemplateViewModel.class);
		budgetTemplateViewModel.getAllBudgets().observe(this, new Observer<List<BudgetTemplate>>()
		{
			@Override public void onChanged(List<BudgetTemplate> budgetTemplates)
			{
				// update our RecyclerView
				adapter.setBudgets(budgetTemplates);
			}
		});

		final ExpensesPage page = this;
		final ProgressBar bar = findViewById(R.id.pb_red_progress);
		progressBarViewModel = ViewModelProviders.of(this).get(ProgressBarViewModel.class);

		progressBarViewModel.getAllIncome().observe(page, new Observer<Double>()
		{
			@Override public void onChanged(final Double income)
			{
				bar.setMax(income.intValue());

				progressBarViewModel.sumBudgets().observe(page, new Observer<Double>()
				{
					@Override public void onChanged(Double budget)
					{
						if (budget == null)
						{
							bar.setProgress(income.intValue());
							x.setText(0.0 + "/" + income + " budgeted");
						}
						else
						{

							bar.setProgress(income.intValue() - budget.intValue());
							x.setText(budget + "/" + income + " budgeted");
						}
			}
		});

			}
		});


		new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
				ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
		{
			@Override public boolean onMove(@NonNull RecyclerView recyclerView,
					@NonNull RecyclerView.ViewHolder viewHolder,
					@NonNull RecyclerView.ViewHolder target)
			{
				return false;
			}

			@Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
					int direction)
			{
				budgetTemplateViewModel.deleteBudget(adapter.getBudget(viewHolder.getAdapterPosition()));
				Toast.makeText(ExpensesPage.this, "Budget Deleted", Toast.LENGTH_SHORT);
			}
		}).attachToRecyclerView(recyclerView);

		adapter.setOnItemClickListener(new AdvavcedMoneyAdapter.OnItemClickListener() {
			@Override
			public void onItemClickListener(BudgetTemplate budget) {
				Intent intent = new Intent(ExpensesPage.this, AddExpensePage.class);
				intent.putExtra(AddExpensePage.EXTRA_ID, budget.getId());
				intent.putExtra(AddExpensePage.EXTRA_CATEGORY, budget.getCategory());
				intent.putExtra(AddExpensePage.EXTRA_AMOUNT, budget.getAmount());
				startActivityForResult(intent,EDIT_BUDGET_REQUEST);
			}
		});
	}

	@Override protected void onActivityResult(int requestCode, int resultCode,
			@Nullable Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ADD_BUDGET_REQUEST && resultCode == RESULT_OK)
		{
			assert data != null;
			String category = data.getStringExtra(AddExpensePage.EXTRA_CATEGORY);
			double amount = data.getDoubleExtra(AddExpensePage.EXTRA_AMOUNT, 0.0);

			BudgetTemplate budgetTemplate = new BudgetTemplate(category, amount);
			budgetTemplateViewModel.insertBudget(budgetTemplate);

			Toast.makeText(this, "Budget Saved", Toast.LENGTH_SHORT).show();
		}
		else if(requestCode == EDIT_BUDGET_REQUEST && resultCode == RESULT_OK){
			int id = data.getIntExtra(AddExpensePage.EXTRA_ID,-1);
			if(id == -1){
				Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
				return;
			}
			String category = data.getStringExtra(AddExpensePage.EXTRA_CATEGORY);
			double amount = data.getDoubleExtra(AddExpensePage.EXTRA_AMOUNT, 0.0);
			BudgetTemplate budgetTemplate = new BudgetTemplate(category, amount);
			budgetTemplate.setId(id);
			budgetTemplateViewModel.updateBudget(budgetTemplate);

			Toast.makeText(this, "Budget Updated", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(this, "Budget Not Saved", Toast.LENGTH_SHORT).show();
		}
	}


	public void mainPage(View view)
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

}
