package com.smartbuilders.smartsales.ecommerce;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jasgcorp.ids.model.User;
import com.smartbuilders.smartsales.ecommerce.data.OrderLineDB;
import com.smartbuilders.smartsales.ecommerce.model.OrderLine;
import com.smartbuilders.smartsales.ecommerce.session.Parameter;

/**
 * Created by stein on 5/1/2016.
 */
public class DialogMoveToShoppingCart extends DialogFragment {

    private static final String STATE_CURRENT_ORDER_LINE = "STATE_CURRENT_ORDER_LINE";
    private static final String STATE_CURRENT_USER = "STATE_CURRENT_USER";

    private OrderLine mOrderLine;
    private User mUser;

    public DialogMoveToShoppingCart() {
        // Empty constructor required for DialogFragment
    }

    public static DialogMoveToShoppingCart newInstance(OrderLine orderLine, User user){
        DialogMoveToShoppingCart dialogMoveToShoppingCart = new DialogMoveToShoppingCart();
        dialogMoveToShoppingCart.mUser = user;
        dialogMoveToShoppingCart.mOrderLine = orderLine;
        return dialogMoveToShoppingCart;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(STATE_CURRENT_ORDER_LINE)){
                mOrderLine = savedInstanceState.getParcelable(STATE_CURRENT_ORDER_LINE);
            }
            if(savedInstanceState.containsKey(STATE_CURRENT_USER)){
                mUser = savedInstanceState.getParcelable(STATE_CURRENT_USER);
            }
        }

        final View view = inflater.inflate(R.layout.dialog_move_to_shopping_cart, container);

        if (Parameter.isManagePriceInOrder(getContext(), mUser)) {
            ((TextView) view.findViewById(R.id.product_price_textView))
                    .setText(getString(R.string.price_detail,
                            mOrderLine.getProduct().getDefaultProductPriceAvailability().getCurrency().getName(),
                            mOrderLine.getProduct().getDefaultProductPriceAvailability().getPrice()));
            view.findViewById(R.id.product_price_textView).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.product_price_textView).setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.product_availability_textView))
                .setText(getContext().getString(R.string.availability,
                        mOrderLine.getProduct().getDefaultProductPriceAvailability().getAvailability()));

        view.findViewById(R.id.cancel_button).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            }
        );

        view.findViewById(R.id.move_to_shopping_cart_button).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int qtyRequested = Integer
                                .valueOf(((EditText) view.findViewById(R.id.qty_requested_editText)).getText().toString());
                        //TODO: mandar estas validaciones a una clase de businessRules
                        if (qtyRequested<=0) {
                            throw new Exception(getString(R.string.invalid_qty_requested));
                        }
                        if ((qtyRequested % mOrderLine.getProduct().getProductCommercialPackage().getUnits())!=0) {
                            throw new Exception(getString(R.string.invalid_commercial_package_qty_requested));
                        }
                        if (qtyRequested > mOrderLine.getProduct().getDefaultProductPriceAvailability().getAvailability()) {
                            throw new Exception(getString(R.string.invalid_availability_qty_requested));
                        }
                        OrderLineDB orderLineDB = new OrderLineDB(getContext(), mUser);
                        String result = orderLineDB.moveOrderLineToShoppingCart(mOrderLine, qtyRequested);
                        if(result == null){
                            Toast.makeText(getContext(), R.string.product_moved_to_shopping_cart, Toast.LENGTH_SHORT).show();
                            ((WishListFragment) getTargetFragment()).reloadWishList();
                        } else {
                            throw new Exception(result);
                        }
                        dismiss();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), getString(R.string.invalid_qty_requested), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        );

        if(view.findViewById(R.id.product_commercial_package) != null){
            if(mOrderLine.getProduct()!=null && mOrderLine.getProduct().getProductCommercialPackage()!=null
                    && !TextUtils.isEmpty(mOrderLine.getProduct().getProductCommercialPackage().getUnitDescription())){
                ((TextView) view.findViewById(R.id.product_commercial_package)).setText(getContext().getString(R.string.commercial_package,
                        mOrderLine.getProduct().getProductCommercialPackage().getUnits(), mOrderLine.getProduct().getProductCommercialPackage().getUnitDescription()));
            }else{
                view.findViewById(R.id.product_commercial_package).setVisibility(TextView.GONE);
            }
        }

        ((TextView) view.findViewById(R.id.product_name_textView)).setText(mOrderLine.getProduct().getName());
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_CURRENT_USER, mUser);
        outState.putParcelable(STATE_CURRENT_ORDER_LINE, mOrderLine);
        super.onSaveInstanceState(outState);
    }
}