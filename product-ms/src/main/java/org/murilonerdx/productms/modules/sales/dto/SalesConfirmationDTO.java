package org.murilonerdx.productms.modules.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.murilonerdx.productms.modules.sales.enums.SalesStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesConfirmationDTO {

	private String salesId;
	private SalesStatus status;
	private String transactionid;
}
